# app/routers/recommend_router.py
from fastapi import APIRouter
from app.services.recommend_service import get_personalized_recommendations

router = APIRouter()  # 각 기능별로 라우트를 분리해서 관리


@router.post("/personalized")
async def personalized_recommendation(request: dict):
    return get_personalized_recommendations(request)
