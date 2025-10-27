from fastapi import FastAPI, Request, Query
from app.core.dependencies import lifespan
from app.models import HomeRecommendRequest, RecommendResponse, SearchResponse
from app.crud import get_ranked_home_recs, get_text_search_results
from app.core.config import settings
from app.models import (
    HomeRecommendRequest,
    RecommendResponse,
    SearchResponse,
    IndexFilterRequest,
    IndexResponse,  # 추가
)
from app.crud import (
    get_ranked_home_recs,
    get_text_search_results,
    index_single_filter,  # 추가
)
from fastapi import HTTPException, status  # 추가

app = FastAPI(lifespan=lifespan)


@app.post("/recommend/home", response_model=RecommendResponse)
async def recommend_home_filters(
    request_body: HomeRecommendRequest, request: Request, page: int = Query(0, ge=0)
):
    """
    홈 화면 필터 추천 (페이징 지원)
    - 200개 Re-Rank 후 캐시, 20개씩 반환
    """
    ids = await get_ranked_home_recs(request, request_body.filter_ids, page)
    return RecommendResponse(recommended_ids=ids)


@app.get("/search", response_model=SearchResponse)
async def search_filters_by_text(
    request: Request, q: str = Query(..., min_length=1), page: int = Query(0, ge=0)
):
    """
    텍스트 기반 필터 검색 (페이징 지원)
    - 20개씩 반환
    """
    ids = await get_text_search_results(request, q, page)
    return SearchResponse(search_results=ids)


@app.get("/health")
async def health_check():
    """헬스 체크 엔드포인트 (EC2 타겟 그룹용)"""
    return {"status": "ok"}


@app.post(
    "/admin/index", response_model=IndexResponse, status_code=status.HTTP_201_CREATED
)
async def admin_index_filter(data: IndexFilterRequest, request: Request):
    """
    (내부용) 새 필터 또는 수정된 필터를 인덱싱합니다.
    Spring Boot 서버가 필터 생성/수정 시 호출합니다.
    """
    try:
        await index_single_filter(request, data)
        return IndexResponse(status="indexed", filter_id=data.filter_id)
    except ValueError as e:
        # 이미지 처리 실패 등
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        print(f"Internal server error during indexing: {e}")
        raise HTTPException(status_code=500, detail="Error during indexing process.")
