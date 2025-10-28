from contextlib import asynccontextmanager
from fastapi import FastAPI
from sentence_transformers import SentenceTransformer
import chromadb
import redis.asyncio as redis
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # CPU 전용 로딩 (EC2)
    print("Loading models and clients (CPU-only, SentenceTransformer)...")

    # 멀티링구얼 CLIP (텍스트/이미지 겸용)
    model_id = "sentence-transformers/clip-ViT-B-32-multilingual-v1"
    app.state.clip_model = SentenceTransformer(model_id, device="cpu")

    # ChromaDB
    app.state.chroma_client = chromadb.HttpClient(
        host=settings.CHROMA_HOST, port=settings.CHROMA_PORT
    )
    app.state.chroma_collection = app.state.chroma_client.get_or_create_collection(
        name=settings.CHROMA_COLLECTION
    )

    # Redis
    app.state.redis_client = await redis.Redis(
        host=settings.REDIS_HOST, port=settings.REDIS_PORT, decode_responses=True
    )

    print("Startup complete.")
    yield

    await app.state.redis_client.close()
    print("Shutdown complete.")
