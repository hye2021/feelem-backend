# 1. Python 3.11-slim 기반
FROM python:3.11-slim

# 2. 작업 디렉토리
WORKDIR /app

# 3. 환경 변수 설정
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# 4. 시스템 패키지 설치 (이미지 처리 및 빌드에 필요)
RUN apt-get update && apt-get install -y \
    build-essential \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/*
# 5. requirements 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 6. 앱 복사
COPY ./app /app/app
COPY .env /app/.env

# 7. FastAPI 서버 실행 (Uvicorn)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
