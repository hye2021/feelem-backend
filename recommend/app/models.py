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
    sticker_types: List[str]


class IndexFilterRequest(BaseModel):
    filter_id: str  # 예: "1235" (Chroma DB는 ID가 문자열)
    image_url: str  # S3의 'editedImageUrl'
    tags: List[str] = []
    color_adjustments: Dict[str, float] = {}
    sticker_summary: Optional[StickerSummary] = None


class IndexResponse(BaseModel):
    status: str
    filter_id: str
