ai-server/
├── venv/ # (Python 가상환경)
├── app/
│ ├── main.py # FastAPI 서버 진입점
│ ├── routers/ # URL 경로 정의
│ │ ├── recommend_router.py
│ │ ├── search_router.py
│ ├── services/ # 실제 로직 (추천/검색 알고리즘)
│ │ ├── recommend_service.py
│ │ ├── search_service.py
│ ├── models/ # 모델 관련 코드 (CLIP 등)
│ │ ├── clip_model.py
│ ├── utils/ # 공용 함수
│ │ ├── similarity.py
│ └── data/ # 학습된 벡터, 메타데이터 저장
│ ├── filter_embeddings.npy # 샘플 벡터
│ ├── metadata.json # 색감/태그/스티커 정보
├── requirements.txt
└── README.md
