# 앱 실행 시 모델과 클라이언트를 한 번만 로드하여 app.state에 저장
from contextlib import asynccontextmanager
from fastapi import FastAPI
from sentence_transformers import SentenceTransformer
import chromadb
import redis.asyncio as redis
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 앱 시작 시
    print("Loading models and clients...")
    app.state.clip_model = SentenceTransformer(settings.CLIP_MODEL_NAME)

    app.state.chroma_client = chromadb.HttpClient(
        host=settings.CHROMA_HOST, port=settings.CHROMA_PORT
    )
    # 컬렉션 가져오기 (없으면 생성 - 실제로는 ETL 스크립트가 생성해야 함)
    app.state.chroma_collection = app.state.chroma_client.get_or_create_collection(
        name=settings.CHROMA_COLLECTION
    )

    app.state.redis_client = await redis.Redis(
        host=settings.REDIS_HOST, port=settings.REDIS_PORT, decode_responses=True
    )
    print("Startup complete.")

    yield

    # 앱 종료 시
    await app.state.redis_client.close()
    print("Shutdown complete.")
