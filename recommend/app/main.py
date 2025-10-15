# app/main.py
from fastapi import FastAPI
from app.routers import recommend_router, search_router

app = FastAPI(title="Feel'em AI Server")

app.include_router(recommend_router.router, prefix="/api/ai/recommend")
app.include_router(search_router.router, prefix="/api/ai/search")


@app.get("/")
def health_check():
    return {"status": "AI Server running!"}
