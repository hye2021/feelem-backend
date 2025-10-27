# Spring Boot와 통신할 DTO
from pydantic import BaseModel
from typing import List, Dict, Any


class HomeRecommendRequest(BaseModel):
    filter_ids: List[str]  # 예: ["123", "456"]


class RecommendResponse(BaseModel):
    recommended_ids: List[str]


class SearchResponse(BaseModel):
    search_results: List[str]
