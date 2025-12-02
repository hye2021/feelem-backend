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
    count: int
    placement_types: List[str]
    has_face_sticker: bool


class IndexFilterRequest(BaseModel):
    filter_id: str
    name: str
    image_url: str
    tags: List[str] = []
    color_adjustments: Dict[str, float] = {}
    sticker_summary: Optional[StickerSummary] = None


class IndexResponse(BaseModel):
    status: str
    filter_id: str
