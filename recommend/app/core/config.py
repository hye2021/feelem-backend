# 환경변수 관리
import os
from pydantic_settings import BaseSettings
from dotenv import load_dotenv
from typing import Optional

load_dotenv()


class Settings(BaseSettings):
    # Chroma DB 설정 (독립 서버로 띄운다고 가정)
    CHROMA_HOST: str = os.getenv("CHROMA_HOST", "localhost")
    CHROMA_PORT: int = int(os.getenv("CHROMA_PORT", "8000"))
    CHROMA_COLLECTION: str = "filters"

    # Redis 설정 (캐싱용)
    REDIS_HOST: str = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT: int = int(os.getenv("REDIS_PORT", 6379))

    # CLIP 모델명
    CLIP_MODEL_NAME: str = "clip-ViT-B-32"

    # Re-Rank 및 페이징 설정
    PRIMARY_QUERY_COUNT: int = 200  # 1차 조회할 개수
    PAGE_SIZE: int = 20  # 페이지당 반환할 개수
    CACHE_EXPIRATION_SECONDS: int = 3600  # 1시간

    # [추가] 로컬 S3 테스트를 위한 자격 증명
    # (배포 시에는 EC2 IAM Role을 사용하므로 None이 됨)
    AWS_ACCESS_KEY_ID: Optional[str] = os.getenv("AWS_ACCESS_KEY_ID")
    AWS_SECRET_ACCESS_KEY: Optional[str] = os.getenv("AWS_SECRET_ACCESS_KEY")
    AWS_REGION: str = "ap-northeast-2"  # (또는 S3 버킷이 있는 리전)


settings = Settings()
