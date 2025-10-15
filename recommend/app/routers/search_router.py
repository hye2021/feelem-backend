# app/routers/search_router.py
from fastapi import APIRouter
from app.services.search_service import get_integrated_search_results

router = APIRouter()


@router.post("/integrated")
async def integrated_search(request: dict):
    return get_integrated_search_results(request)
