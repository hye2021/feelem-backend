# app/main.py
from fastapi import FastAPI, HTTPException, Body

# from fastapi.responses import StreamingResponse # (삭제)
from contextlib import asynccontextmanager
from typing import List

# import io # (삭제)
import os
from dotenv import load_dotenv  # .env 파일 로드

# --- 모델 및 캐시 임포트 ---
from app.models import (
    IndexRequest,
    IndexResponse,
    ForYouRequest,
    RecommendationResponse,
    SearchRequest,
    # StickerRequest, # (삭제)
)
import app.services as services
import app.cache as cache
from pydantic import BaseModel


class CacheUpdateResponse(BaseModel):
    success: bool
    filter_id: int


# --- FastAPI 앱 생명주기 설정 ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 앱 시작 시
    print("Application startup...")
    # 1. .env 파일 로드 (가장 먼저!)
    load_dotenv()
    # 2. RDBMS에서 캐시 로드
    cache.load_caches_from_db()
    # 3. AI 모델 로드 (services.py에서 이미 로드됨)
    print(f"Recommendation Models loaded on {services.DEVICE}.")  # (로그 수정)
    yield
    # 앱 종료 시
    print("Application shutdown...")


app = FastAPI(lifespan=lifespan)

# --- API 엔드포인트 ---


@app.get("/")
def read_root():
    return {
        "message": "Feel'em Recommendation Server is running locally"
    }  # (메시지 수정)


@app.post("/index", response_model=IndexResponse)
async def api_index_filter(request: IndexRequest):
    """(Postman 테스트용) 필터 인덱싱"""
    success = services.index_filter(request.filter_id, request.edited_image_url)
    if not success:
        raise HTTPException(status_code=500, detail="Failed to index filter")
    return IndexResponse(success=True, filter_id=request.filter_id)


# --- (api_generate_sticker 함수 전체 삭제) ---


@app.post("/recommend/for-you", response_model=RecommendationResponse)
async def api_get_for_you(request: ForYouRequest = Body(...)):
    """(PostVman 테스트용) For You 추천"""
    profiles = services.get_user_profiles(request.recent_filter_ids)
    if profiles["visual_profile"] is None:
        return RecommendationResponse(filter_ids=[])  # 콜드 스타트

    candidates = services.filter_collection.query(
        query_embeddings=[profiles["visual_profile"].tolist()],
        n_results=50,
        include=["distances"],
    )

    candidate_list = [
        {"id": cid, "distance": dist}
        for cid, dist in zip(candidates["ids"][0], candidates["distances"][0])
        if int(cid) not in request.recent_filter_ids
    ]

    weights = {"visual": 0.7, "pop": 0.1, "sticker": 0.2}
    final_ids = services.re_rank_filters(
        candidate_list, profiles["sticker_profile"], weights
    )

    return RecommendationResponse(filter_ids=final_ids[:20])


@app.post("/search", response_model=RecommendationResponse)
async def api_search(request: SearchRequest = Body(...)):
    """(Postman 테스트용) 텍스트 검색"""
    query_vector = services.clip_model.encode(request.query_text).tolist()

    candidates = services.filter_collection.query(
        query_embeddings=[query_vector], n_results=50, include=["distances"]
    )

    candidate_list = [
        {"id": cid, "distance": dist}
        for cid, dist in zip(candidates["ids"][0], candidates["distances"][0])
    ]

    profiles = services.get_user_profiles(request.recent_filter_ids)
    weights = {"visual": 0.8, "pop": 0.1, "sticker": 0.1}
    final_ids = services.re_rank_filters(
        candidate_list, profiles["sticker_profile"], weights
    )

    return RecommendationResponse(filter_ids=final_ids[:20])


# --- 캐시 관련 엔드포인트 ---
@app.post("/cache/refresh/{filter_id}", response_model=CacheUpdateResponse)
async def api_refresh_cache(filter_id: int):
    """
    (Spring 서버가 호출)
    필터가 새로 생성되거나, 저장/사용 카운트가 변경될 때 호출됩니다.
    해당 필터 ID의 캐시를 즉시 새로고침합니다.
    """
    success = cache.update_single_filter_cache(filter_id)
    if not success:
        raise HTTPException(status_code=500, detail="Failed to update cache")
    return CacheUpdateResponse(success=True, filter_id=filter_id)
