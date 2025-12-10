import requests
import numpy as np
from PIL import Image
from io import BytesIO
from typing import List
from fastapi import Request, HTTPException
from app.core.config import settings
from app.services.ranking import re_rank_candidates
from app.models import IndexFilterRequest
from fastapi.concurrency import run_in_threadpool
from urllib.parse import urlparse
import boto3
import json

# ----------------------------
# S3 클라이언트
# ----------------------------
if settings.AWS_ACCESS_KEY_ID and settings.AWS_SECRET_ACCESS_KEY:
    # print("Initializing S3 client with Access Keys (Local Test Mode)")
    s3_client = boto3.client(
        "s3",
        aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
        aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
        region_name=settings.AWS_REGION,
    )
else:
    # print("Initializing S3 client with IAM Role (Production Mode)")
    s3_client = boto3.client("s3", region_name=settings.AWS_REGION)


# ----------------------------
# 홈 추천 (Re-Rank)
# ----------------------------
async def get_ranked_home_recs(
    request: Request, filter_ids: List[str], page: int, size: int = 20
) -> List[str]:

    # 1. ID가 없으면 DB 조회를 아예 시도하지 않음 (Cold Start 방어)
    if not filter_ids:
        return []

    redis = request.app.state.redis_client
    chroma_collection = request.app.state.chroma_collection
    sorted_ids = sorted(filter_ids)
    cache_key = f"recs:home:{settings.CHROMA_COLLECTION}:{':'.join(sorted_ids)}"

    ranked_ids: List[str] = []

    try:
        cached = await redis.get(cache_key)
        if cached:
            ranked_ids = json.loads(cached)
        else:
            inputs = await run_in_threadpool(
                chroma_collection.get,
                ids=sorted_ids,
                include=["embeddings", "metadatas"],
            )

            # 2. 2차 방어: ID는 보냈는데 DB에 데이터가 없을 때
            embeddings = inputs.get("embeddings") if inputs else None
            if embeddings is None or len(embeddings) == 0:
                return []

            avg_vec = np.mean(inputs["embeddings"], axis=0).tolist()

            cands = await run_in_threadpool(
                chroma_collection.query,
                query_embeddings=[avg_vec],
                n_results=settings.PRIMARY_QUERY_COUNT,
                include=["metadatas", "distances"],
            )
            ranked_ids = re_rank_candidates(inputs.get("metadatas", []), cands)

            if ranked_ids:
                await redis.setex(
                    cache_key, settings.CACHE_EXPIRATION_SECONDS, json.dumps(ranked_ids)
                )
    except Exception as e:
        print(f"❌ Error during home recommendation: {e}")
        return []  # 에러 발생 시에도 죽지 않게 빈 리스트 반환

    start = page * size
    end = start + size
    return ranked_ids[start:end]


# ----------------------------
# 텍스트 검색 (수정됨: size 파라미터 적용)
# ----------------------------
async def get_text_search_results(
    request: Request,
    query: str,
    page: int,
    size: int = settings.PAGE_SIZE,
) -> List[str]:

    model = request.app.state.clip_model
    chroma_collection = request.app.state.chroma_collection

    # 1. 최대 검색 개수 제한 (200개)
    max_limit = settings.PRIMARY_QUERY_COUNT

    start = page * size
    end = start + size

    # 2. 요청한 페이지의 시작점이 제한(200개)을 넘으면 빈 리스트 반환
    if start >= max_limit:
        return []

    try:
        text_vec = await run_in_threadpool(
            lambda: model.encode(sentences=[query], convert_to_numpy=True)[0].tolist()
        )

        # 3. ChromaDB 조회: 항상 max_limit(200개)까지 조회
        results = await run_in_threadpool(
            chroma_collection.query,
            query_embeddings=[text_vec],
            n_results=max_limit,
        )

        ids = results.get("ids", [[]])[0]
        if not ids:
            return []

        # 4. 메모리 슬라이싱 (입력받은 size 기준)
        return ids[start:end]

    except Exception as e:
        print(f"❌ Error during text search: {e}")
        # 검색 실패 시 500 에러보다는 빈 결과를 주는 것이 안전할 수 있음
        return []


# ----------------------------
# 이미지 → 벡터 (V7: Numpy 변환)
# ----------------------------
def _fetch_and_vectorize_sync(model, image_url: str) -> List[float]:
    """
    공개 S3(HTTP) 우선, 실패 시 boto3로 private S3 시도.
    이미지(PIL)를 Numpy 배열로 변환하여 encode함.
    """
    img_bytes = None
    try:
        r = requests.get(image_url, timeout=5)
        r.raise_for_status()
        img_bytes = r.content
    except requests.exceptions.RequestException:
        try:
            parsed = urlparse(image_url)
            bucket = parsed.netloc.split(".")[0]
            key = parsed.path.lstrip("/")
            obj = s3_client.get_object(Bucket=bucket, Key=key)
            img_bytes = obj["Body"].read()
        except Exception as s3_e:
            raise ValueError(f"Failed to fetch image: {image_url} ({s3_e})")

    image = Image.open(BytesIO(img_bytes)).convert("RGB")
    img_array = np.asarray(image)
    vec_batch = model.encode(sentences=[img_array], convert_to_numpy=True)
    vec = vec_batch[0].tolist()
    return vec


# ----------------------------
# 단일 필터 인덱싱
# ----------------------------
async def index_single_filter(request: Request, data: IndexFilterRequest) -> None:
    chroma_collection = request.app.state.chroma_collection
    model = request.app.state.clip_model

    # 1. 이미지 벡터화
    try:
        image_vector = await run_in_threadpool(
            _fetch_and_vectorize_sync, model, data.image_url
        )
    except Exception as e:
        print(f"❌ Failed to process image for filter_id {data.filter_id}: {e}")
        raise HTTPException(
            status_code=400,
            detail=f"Failed to process image: {e}",
        )

    # 2. 메타데이터 생성 (JSON 직렬화 필수)
    metadata = {
        "name": data.name,
        "tags": ", ".join(data.tags) if data.tags else "",
        "color_adjustments": json.dumps(data.color_adjustments),
    }

    if data.sticker_summary:
        # Pydantic 모델 -> Dict -> JSON 문자열 변환
        metadata["sticker_summary"] = json.dumps(data.sticker_summary.model_dump())

    # 3. DB 저장 (Upsert)
    try:
        await run_in_threadpool(
            chroma_collection.upsert,
            ids=[data.filter_id],
            embeddings=[image_vector],
            metadatas=[metadata],
        )
        print(f"✅ Indexed filter: {data.filter_id} (Name: {data.name})")
    except Exception as e:
        print(f"❌ Failed to upsert filter {data.filter_id}: {e}")
        raise HTTPException(status_code=500, detail="ChromaDB upsert failed.")
