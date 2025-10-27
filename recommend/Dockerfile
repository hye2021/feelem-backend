# 1. 베이스 이미지 선택 (Python 3.11-slim 사용)
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /code

# 3. 환경변수 설정 (Python이 로그를 버퍼링하지 않도록 설정)
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# 4. (중요) 의존성 설치
# requirements.txt만 먼저 복사하여 pip install을 실행합니다.
# 이렇게 하면 소스 코드가 변경되어도 이 레이어는 캐시되어 빌드 속도가 빨라집니다.
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade -r requirements.txt

# 5. 애플리케이션 코드 복사
COPY ./app /code/app

# 6. (선택) .env 파일 복사 - 권장되지는 않음
# COPY .env .
# -> 더 좋은 방법: ECR에 push하고 EC2에서 run 할 때 -e 또는 --env-file 옵션으로 주입

# 7. 서버 실행
# AWS EC2의 기본 HTTP 포트인 80번을 사용합니다.
EXPOSE 80
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"]