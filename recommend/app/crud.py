# app/crud.py

import requests
import numpy as np
from PIL import Image
from io import BytesIO
from typing import List, Dict
from fastapi import Request
from app.core.config import settings
from app.services.ranking import re_rank_candidates
from app.models import IndexFilterRequest
import boto3
from urllib.parse import urlparse
from fastapi.concurrency import run_in_threadpool
import json

# [수정] s3_client 초기화 로직 (정리된 버전)
if settings.AWS_ACCESS_KEY_ID and settings.AWS_SECRET_ACCESS_KEY:
    print("Initializing S3 client with Access Keys (Local Test Mode)")
    s3_client = boto3.client(
        "s3",
        aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
        aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
        region_name=settings.AWS_REGION,
    )
else:
    print("Initializing S3 client with IAM Role (Production Mode)")
    s3_client = boto3.client("s3", region_name=settings.AWS_REGION)


# --- [누락되었던 함수 1] ---
async def get_ranked_home_recs(
    request: Request, filter_ids: List[str], page: int
) -> List[str]:
    """
    홈 추천: 200개 조회 -> Re-Rank -> 캐싱 -> 20개씩 페이징
    """
    redis = request.app.state.redis_client
    chroma_collection = request.app.state.chroma_collection

    sorted_ids = sorted(filter_ids)
    cache_key = f"recs:home:{':'.join(sorted_ids)}"

    # 1. 캐시 조회 (비동기)
    cached_data = await redis.get(cache_key)

    ranked_ids = []

    if cached_data:
        # 캐시 히트
        ranked_ids = json.loads(cached_data)
    else:
        # 캐시 미스
        try:
            # 2. 입력 필터 벡터 조회 (동기 I/O -> 스레드 풀)
            input_filters = await run_in_threadpool(
                chroma_collection.get,
                ids=sorted_ids,
                include=["embeddings", "metadatas"],
            )

            if not input_filters or not input_filters.get("embeddings"):
                return []

            # 3. 평균 벡터 계산 (CPU-bound, 빠름)
            avg_image_vector = np.mean(input_filters["embeddings"], axis=0).tolist()

            # 4. 유사 필터 200개 조회 (동기 I/O -> 스레드 풀)
            candidates = await run_in_threadpool(
                chroma_collection.query,
                query_embeddings=[avg_image_vector],
                n_results=settings.PRIMARY_QUERY_COUNT,
                include=["metadatas", "distances"],
            )

            # 5. Re-Rank (CPU-bound, 빠름)
            input_metadatas = input_filters.get("metadatas", [])
            ranked_ids = re_rank_candidates(input_metadatas, candidates)

            if ranked_ids:
                # 6. 캐시 저장 (비동기)
                await redis.set(
                    cache_key,
                    json.dumps(ranked_ids),
                    ex=settings.CACHE_EXPIRATION_SECONDS,
                )

        except Exception as e:
            print(f"Error during home recommendation: {e}")
            return []

    # 7. 페이징 (빠름)
    start_index = page * settings.PAGE_SIZE
    end_index = start_index + settings.PAGE_SIZE

    return ranked_ids[start_index:end_index]


# --- [누락되었던 함수 2] ---
async def get_text_search_results(request: Request, query: str, page: int) -> List[str]:
    """
    텍스트 검색: 페이징 지원
    """
    clip_model = request.app.state.clip_model
    chroma_collection = request.app.state.chroma_collection

    try:
        # 1. 텍스트 벡터화 (동기, CPU-heavy -> 스레드 풀)
        text_vector = await run_in_threadpool(clip_model.encode, query)

        # 2. ChromaDB 검색 (동기 I/O -> 스레드 풀)
        offset = page * settings.PAGE_SIZE

        results = await run_in_threadpool(
            chroma_collection.query,
            query_embeddings=[text_vector.tolist()],
            n_results=settings.PAGE_SIZE,
            offset=offset,
        )

        if not results or not results.get("ids") or not results["ids"][0]:
            return []

        return results["ids"][0]

    except Exception as e:
        print(f"Error during text search: {e}")
        return []


# --- [기존에 있던 함수들] ---


def _fetch_and_vectorize_sync(clip_model, image_url: str) -> List[float]:
    """
    (동기) S3 URL에서 이미지를 다운로드하고 벡터화합니다.
    이 함수는 스레드 풀에서 실행되어야 합니다.
    """
    try:
        # 1. Public S3 시도 (requests)
        response = requests.get(image_url, timeout=5)
        response.raise_for_status()  # 오류 시 예외 발생
        image_bytes = response.content

    except requests.exceptions.RequestException as e:
        # Public 접근 실패 시 (e.g., 403 Forbidden)
        print(f"Public S3 access failed ({e}). Trying Private S3 (boto3)...")
        try:
            # 2. Private S3 시도 (boto3)
            parsed_url = urlparse(image_url)
            bucket_name = parsed_url.hostname.split(".")[0]
            object_key = parsed_url.path.lstrip("/")

            s3_response = s3_client.get_object(Bucket=bucket_name, Key=object_key)
            image_bytes = s3_response["Body"].read()

        except Exception as s3_e:
            print(f"Private S3 (boto3) access also failed: {s3_e}")
            raise ValueError(f"Failed to fetch image from S3: {image_url}")

    # 3. 이미지 디코딩 및 벡터화 (CPU 집약적 작업)
    image = Image.open(BytesIO(image_bytes))
    image_vector = clip_model.encode(image).tolist()
    return image_vector


def _format_metadata(data: IndexFilterRequest) -> dict:
    """Chroma DB에 저장할 메타데이터를 포맷팅합니다."""
    metadata = {"tags": data.tags, "color_adjustments": data.color_adjustments}
    if data.sticker_summary:
        metadata["sticker_summary"] = data.sticker_summary.model_dump()
    return metadata


async def index_single_filter(request: Request, data: IndexFilterRequest) -> None:
    """
    단일 필터 정보를 받아 Chroma DB에 인덱싱(upsert)합니다.
    """
    chroma_collection = request.app.state.chroma_collection
    clip_model = request.app.state.clip_model

    # 1. 이미지 벡터화 (스레드 풀에서 실행)
    try:
        image_vector = await run_in_threadpool(
            _fetch_and_vectorize_sync,  # 동기 함수
            clip_model=clip_model,  # 인자 전달
            image_url=data.image_url,
        )
    except Exception as e:
        raise ValueError(
            f"Failed to process image for filter_id: {data.filter_id}. Error: {e}"
        )

    # 2. 메타데이터 포맷팅
    metadata = _format_metadata(data)

    # 3. Chroma DB Upsert (스레드 풀에서 실행)
    await run_in_threadpool(
        chroma_collection.upsert,  # 동기 함수
        ids=[data.filter_id],  # 인자 전달
        embeddings=[image_vector],
        metadatas=[metadata],
    )
    print(f"Successfully indexed filter: {data.filter_id}")
