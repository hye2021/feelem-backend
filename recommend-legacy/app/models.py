# Pydentic 라이브러리의 BaseModel을 상속받아 만들어 짐
# Dto 역할
# 1. 데이터 유효성 검사
# 2. 응답 형식 보장
# 3. API 문서 자동 생성 (http://127.0.0.1:8000/docs)


# app/models.py
from pydantic import BaseModel
from typing import List


# --- Indexing ---
class IndexRequest(BaseModel):
    filter_id: int
    edited_image_url: str


class IndexResponse(BaseModel):
    success: bool
    filter_id: int


# --- For You ---
class ForYouRequest(BaseModel):
    user_id: str  # 또는 int
    recent_filter_ids: List[int]


class RecommendationResponse(BaseModel):
    filter_ids: List[int]


# --- Search ---
class SearchRequest(BaseModel):
    user_id: str  # 또는 int
    query_text: str
    recent_filter_ids: List[int]  # 스티커 부스팅을 위함
