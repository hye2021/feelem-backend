# Spring Boot와 통신할 DTO
from pydantic import BaseModel
from typing import List, Dict, Any, Optional


class HomeRecommendRequest(BaseModel):
    filter_ids: List[str]  # 예: ["123", "456"]


class RecommendResponse(BaseModel):
    recommended_ids: List[str]


class SearchResponse(BaseModel):
    search_results: List[str]


class StickerSummary(BaseModel):
    count: int  # 얼굴 인식 스티커 개수
    placement_types: List[
        str
    ]  # 얼굴 인식 스티커 배치 위치 (예: "LEFT_EYE", "TOP_HEAD")
    has_face_sticker: bool  # 얼굴 인식 스티커 사용 여부 (True/False)
    # sticker_types: List[str]  <-- 삭제됨


class IndexFilterRequest(BaseModel):
    filter_id: str
    image_url: str
    tags: List[str] = []
    color_adjustments: Dict[str, float] = {}
    sticker_summary: Optional[StickerSummary] = None


class IndexResponse(BaseModel):
    status: str
    filter_id: str
