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
# ✅ S3 클라이언트
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

    # 🛑 ID가 없으면 DB 조회를 아예 시도하지 않아야 함
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

            # 2. 여기는 "ID는 보냈는데 DB에 그 ID 데이터가 없을 때"를 위한 2차 방어입니다.
            if not inputs or not inputs.get("embeddings"):
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
        # raise HTTPException(status_code=500...) # 굳이 에러 내지 말고 빈 리스트 주는 게 안전할 수 있음
        return []  # 에러 발생 시에도 죽지 않게 빈 리스트 반환

    # ✅ 항상 파이썬에서 최종 페이징
    start = page * settings.PAGE_SIZE
    end = start + settings.PAGE_SIZE
    return ranked_ids[start:end]


# ----------------------------
# ✅ 텍스트 검색 (리스트 입력 + [0] 추출) - 페이징 최적화 적용됨
# ----------------------------
async def get_text_search_results(request: Request, query: str, page: int) -> List[str]:
    model = request.app.state.clip_model
    chroma_collection = request.app.state.chroma_collection

    # 1. 최대 검색 개수 제한 (Config의 PRIMARY_QUERY_COUNT, 예: 200)
    max_limit = settings.PRIMARY_QUERY_COUNT
    start = page * settings.PAGE_SIZE
    end = start + settings.PAGE_SIZE

    # 2. 요청한 페이지의 시작점이 제한(200개)을 넘으면 검색하지 않고 빈 리스트 반환
    # (불필요한 DB 조회 및 연산 방지)
    if start >= max_limit:
        return []

    try:
        # 멀티링구얼 ST 모델: 리스트 입력 후 첫 벡터만 사용
        text_vec = await run_in_threadpool(
            lambda: model.encode(sentences=[query], convert_to_numpy=True)[0].tolist()
        )

        # 3. ChromaDB 조회: 페이지가 늘어나도 항상 max_limit(200개)까지만 조회
        # (기존 코드: settings.PAGE_SIZE * (page + 1) -> 페이지 갈수록 계속 늘어남 문제 해결)
        results = await run_in_threadpool(
            chroma_collection.query,
            query_embeddings=[text_vec],
            n_results=max_limit,
        )

        ids = results.get("ids", [[]])[0]
        if not ids:
            return []

        # 4. 메모리 상에서 슬라이싱하여 해당 페이지 분량만 반환
        return ids[start:end]

    except Exception as e:
        print(f"❌ Error during text search: {e}")
        raise HTTPException(status_code=500, detail="Text search failed.")


# ----------------------------
# ✅ 이미지 → 벡터 (V7: Numpy 변환)
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
        # Private S3 시도
        try:
            parsed = urlparse(image_url)
            bucket = parsed.netloc.split(".")[0]
            key = parsed.path.lstrip("/")
            obj = s3_client.get_object(Bucket=bucket, Key=key)
            img_bytes = obj["Body"].read()
        except Exception as s3_e:
            raise ValueError(f"Failed to fetch image: {image_url} ({s3_e})")

    # Pillow 이미지 열기 + RGB 변환
    image = Image.open(BytesIO(img_bytes)).convert("RGB")

    # print("---!!! V7 (numpy array) IS RUNNING !!!---") # 디버깅용 로그

    # ✅ V7 Fix: PIL 이미지를 Numpy 배열로 변환
    img_array = np.asarray(image)

    # Numpy 배열을 리스트로 감싸서 'sentences' 인자에 전달
    vec_batch = model.encode(sentences=[img_array], convert_to_numpy=True)

    # 2D 배치 결과에서 [0] (1D 벡터)를 추출
    vec = vec_batch[0].tolist()
    return vec


# ----------------------------
# ✅ 단일 필터 인덱싱
# ----------------------------
async def index_single_filter(request: Request, data: IndexFilterRequest) -> None:
    chroma_collection = request.app.state.chroma_collection
    model = request.app.state.clip_model

    try:
        image_vector = await run_in_threadpool(
            _fetch_and_vectorize_sync, model, data.image_url
        )
    except Exception as e:
        # V7 수정으로 인해 여기서 발생하던 'subscriptable' 오류가 해결되었습니다.
        print(f"❌ Failed to process image for filter_id {data.filter_id}: {e}")
        raise HTTPException(
            status_code=400,
            detail=f"Failed to process image for filter_id {data.filter_id}: {e}",
        )

    # Chroma 메타는 원시 타입만 허용 → 문자열로 직렬화
    metadata = {
        "tags": ", ".join(data.tags) if data.tags else "",
        "color_adjustments": json.dumps(data.color_adjustments),
    }
    if data.sticker_summary:
        metadata["sticker_summary"] = json.dumps(data.sticker_summary.model_dump())

    try:
        await run_in_threadpool(
            chroma_collection.upsert,
            ids=[data.filter_id],
            embeddings=[image_vector],  # ChromaDB는 2D 리스트를 기대 [[...]]
            metadatas=[metadata],
        )
        print(f"✅ Indexed filter: {data.filter_id}")
    except Exception as e:
        print(f"❌ Failed to upsert filter {data.filter_id}: {e}")
        raise HTTPException(status_code=500, detail="ChromaDB upsert failed.")
