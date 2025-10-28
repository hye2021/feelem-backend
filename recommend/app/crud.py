import requests
import numpy as np
from PIL import Image, UnidentifiedImageError
from io import BytesIO
from typing import List, Dict
from fastapi import Request, HTTPException
from app.core.config import settings
from app.services.ranking import re_rank_candidates
from app.models import IndexFilterRequest
from fastapi.concurrency import run_in_threadpool
from urllib.parse import urlparse
import boto3
import json
import re


# ----------------------------
# ✅ S3 클라이언트 초기화
# ----------------------------
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


# ----------------------------
# ✅ 홈 추천 (Re-Rank)
# ----------------------------
async def get_ranked_home_recs(
    request: Request, filter_ids: List[str], page: int
) -> List[str]:
    redis = request.app.state.redis_client
    chroma_collection = request.app.state.chroma_collection
    sorted_ids = sorted(filter_ids)
    cache_key = f"recs:home:{settings.CHROMA_COLLECTION}:{':'.join(sorted_ids)}"

    ranked_ids = []

    # 캐시 조회
    try:
        cached_data = await redis.get(cache_key)
        if cached_data:
            return json.loads(cached_data)
    except Exception:
        print("⚠️ Redis connection failed, proceeding without cache.")

    try:
        # 입력 필터 벡터 조회
        input_filters = await run_in_threadpool(
            chroma_collection.get,
            ids=sorted_ids,
            include=["embeddings", "metadatas"],
        )

        if not input_filters or not input_filters.get("embeddings"):
            return []

        avg_vector = np.mean(input_filters["embeddings"], axis=0).tolist()

        candidates = await run_in_threadpool(
            chroma_collection.query,
            query_embeddings=[avg_vector],
            n_results=settings.PRIMARY_QUERY_COUNT,
            include=["metadatas", "distances"],
        )

        input_metadatas = input_filters.get("metadatas", [])
        ranked_ids = re_rank_candidates(input_metadatas, candidates)

        # 캐시 저장 (비동기)
        if ranked_ids:
            try:
                await redis.setex(
                    cache_key,
                    settings.CACHE_EXPIRATION_SECONDS,
                    json.dumps(ranked_ids),
                )
            except Exception:
                print("⚠️ Failed to cache ranked_ids to Redis.")

    except Exception as e:
        print(f"❌ Error during home recommendation: {e}")
        raise HTTPException(status_code=500, detail="Home recommendation failed.")

    start = page * settings.PAGE_SIZE
    end = start + settings.PAGE_SIZE
    return ranked_ids[start:end]


# ----------------------------
# ✅ 텍스트 기반 검색
# ----------------------------
async def get_text_search_results(request: Request, query: str, page: int) -> List[str]:
    clip_model = request.app.state.clip_model
    chroma_collection = request.app.state.chroma_collection
    try:
        # 1️⃣ 텍스트 임베딩
        text_vector = await run_in_threadpool(lambda: clip_model.encode([query])[0])
        text_vector = np.array(text_vector, dtype=np.float32).tolist()

        # 2️⃣ 검색 (offset 제거)
        results = await run_in_threadpool(
            chroma_collection.query,
            query_embeddings=[text_vector],
            n_results=settings.PAGE_SIZE * (page + 1),
        )

        if not results or not results.get("ids") or not results["ids"][0]:
            return []

        # 3️⃣ Python에서 직접 페이징
        start = page * settings.PAGE_SIZE
        end = start + settings.PAGE_SIZE
        return results["ids"][0][start:end]

    except Exception as e:
        print(f"❌ Error during text search: {e}")
        raise HTTPException(status_code=500, detail="Text search failed.")


# ----------------------------
# ✅ 이미지 → 벡터 변환
# ----------------------------
def _fetch_and_vectorize_sync(clip_model, image_url: str) -> List[float]:
    """S3 또는 외부 URL에서 이미지를 받아 CLIP 벡터로 변환"""
    image_bytes = None

    try:
        # 1️⃣ 일반 URL 접근
        response = requests.get(image_url, timeout=5)
        response.raise_for_status()
        image_bytes = response.content
    except requests.exceptions.RequestException:
        # 2️⃣ Private S3 접근
        try:
            match = re.match(r"https?://([^/]+)/(.+)", image_url)
            if not match:
                raise ValueError("Invalid S3 URL format.")
            host, path = match.groups()
            bucket_name = host.split(".")[0]
            object_key = path

            s3_response = s3_client.get_object(Bucket=bucket_name, Key=object_key)
            image_bytes = s3_response["Body"].read()
        except Exception as s3_e:
            raise ValueError(f"Failed to fetch image: {image_url} ({s3_e})")

    try:
        image = Image.open(BytesIO(image_bytes)).convert("RGB")
    except UnidentifiedImageError:
        raise ValueError(f"Invalid image format: {image_url}")

    image_vector = clip_model.encode(image).tolist()
    return image_vector


# ----------------------------
# ✅ 단일 필터 인덱싱 (Spring 호출)
# ----------------------------
async def index_single_filter(request: Request, data: IndexFilterRequest) -> None:
    """
    단일 필터 정보를 받아 Chroma DB에 인덱싱(upsert)합니다.
    """
    chroma_collection = request.app.state.chroma_collection
    clip_model = request.app.state.clip_model

    try:
        image_vector = await run_in_threadpool(
            _fetch_and_vectorize_sync, clip_model, data.image_url
        )
    except Exception as e:
        raise ValueError(f"Failed to process image for filter_id {data.filter_id}: {e}")

    # ✅ metadata 변환 (ChromaDB 호환 형태)
    metadata = {
        "tags": ", ".join(data.tags) if data.tags else "",
        "color_adjustments": json.dumps(data.color_adjustments),
    }

    if data.sticker_summary:
        metadata["sticker_summary"] = json.dumps(data.sticker_summary.model_dump())

    # ✅ upsert 수행
    try:
        await run_in_threadpool(
            chroma_collection.upsert,
            ids=[data.filter_id],
            embeddings=[image_vector],
            metadatas=[metadata],
        )
        print(f"✅ Indexed filter: {data.filter_id}")
    except Exception as e:
        print(f"❌ Failed to upsert filter {data.filter_id}: {e}")
        raise HTTPException(status_code=500, detail="ChromaDB upsert failed.")
