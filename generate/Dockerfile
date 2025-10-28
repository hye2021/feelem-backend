# 1. CUDA 지원 베이스 이미지 (PyTorch + CUDA)
FROM pytorch/pytorch:2.3.0-cuda12.1-cudnn8-runtime

WORKDIR /app

# 2. 필요한 라이브러리 설치
COPY app/requirements.txt .
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 3. 앱 복사
COPY app /app

# 4. 환경 변수
ENV HUGGINGFACE_TOKEN="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"
EXPOSE 8000

# 5. 실행 명령
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
