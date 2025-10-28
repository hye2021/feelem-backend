from fastapi import FastAPI, Request, Query, HTTPException, status
from app.core.dependencies import lifespan
from app.models import (
    HomeRecommendRequest,
    RecommendResponse,
    SearchResponse,
    IndexFilterRequest,
    IndexResponse,
)
from app.crud import (
    get_ranked_home_recs,
    get_text_search_results,
    index_single_filter,
)
from app.core.config import settings

app = FastAPI(lifespan=lifespan)


@app.post("/recommend/home", response_model=RecommendResponse)
async def recommend_home_filters(
    request_body: HomeRecommendRequest,
    request: Request,
    page: int = Query(0, ge=0),
):
    """홈 화면 추천"""
    ids = await get_ranked_home_recs(request, request_body.filter_ids, page)
    return RecommendResponse(recommended_ids=ids)


@app.get("/search", response_model=SearchResponse)
async def search_filters_by_text(
    request: Request,
    q: str = Query(..., min_length=1),
    page: int = Query(0, ge=0),
):
    """텍스트 기반 필터 검색"""
    ids = await get_text_search_results(request, q, page)
    return SearchResponse(search_results=ids)


@app.post(
    "/admin/index",
    response_model=IndexResponse,
    status_code=status.HTTP_201_CREATED,
)
async def admin_index_filter(data: IndexFilterRequest, request: Request):
    """Spring Boot → FastAPI 인덱싱 요청"""
    try:
        await index_single_filter(request, data)
        return IndexResponse(status="indexed", filter_id=data.filter_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        print(f"Internal server error during indexing: {e}")
        raise HTTPException(status_code=500, detail="Error during indexing process.")


@app.get("/health")
async def health_check():
    """ELB TargetGroup Health Check"""
    return {"status": "ok"}
