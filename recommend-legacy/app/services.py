# app/services.py
import chromadb
import requests
import numpy as np
from PIL import Image
from io import BytesIO
from sentence_transformers import SentenceTransformer
from typing import List, Dict, Any

# --- AI 모델 ---
import torch

# (참고: sentence-transformers가 내부적으로 torch를 사용합니다)

# --- 캐시 임포트 ---
from app.cache import get_sticker_meta, get_popularity_score

# --- 1. 전역 변수 및 모델 초기화 ---

# (중요) 로컬 환경에 NVIDIA GPU가 있는지 자동 감지
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Recommendation Service: Using device: {DEVICE}")

# CLIP 모델 (추천/검색)
print("Recommendation Service: Loading CLIP Model (this may take a while)...")
clip_model = SentenceTransformer("clip-ViT-B-32")
# sentence-transformers는 DEVICE("cuda" or "cpu")를 자동으로 감지하여 사용합니다.
print("Recommendation Service: CLIP Model Loaded.")


# Chroma DB (로컬 파일 기반)
db_client = chromadb.PersistentClient(path="./chroma_data")  # ./chroma_data 폴더에 저장
filter_collection = db_client.get_or_create_collection(
    name="filters", metadata={"hnsw:space": "cosine"}  # 코사인 유사도
)

# --- 2. 핵심 로직: 인덱싱 ---


def index_filter(filter_id: int, image_url: str):
    try:
        # S3에서 이미지 다운로드
        response = requests.get(image_url)
        response.raise_for_status()
        img = Image.open(BytesIO(response.content))

        # 이미지를 벡터로 변환
        image_vector = clip_model.encode(img).tolist()

        # Vector DB에 저장
        filter_collection.upsert(
            ids=[str(filter_id)],
            embeddings=[image_vector],
            metadatas=[{"filter_id": filter_id}],
        )
        return True
    except Exception as e:
        print(f"Error indexing filter {filter_id}: {e}")
        return False


# --- 3. 핵심 로직: 사용자 프로필 생성 ---


def get_user_profiles(recent_filter_ids: List[int]) -> Dict[str, Any]:
    if not recent_filter_ids:
        return {
            "visual_profile": None,
            "sticker_profile": {
                "pref_ai": 0.0,
                "pref_brush": 0.0,
                "pref_image": 0.0,
                "pref_face": 0.0,
            },
        }

    # A. 시각 프로필 (평균 벡터)
    vectors = filter_collection.get(
        ids=[str(fid) for fid in recent_filter_ids], include=["embeddings"]
    )

    if not vectors or vectors["embeddings"] is None or len(vectors["embeddings"]) == 0:
        return {
            "visual_profile": None,
            "sticker_profile": {
                "pref_ai": 0.0,
                "pref_brush": 0.0,
                "pref_image": 0.0,
                "pref_face": 0.0,
            },
        }

    visual_profile = np.mean(vectors["embeddings"], axis=0)

    # --- [수정] ---
    # B. 스티커 프로필 (선호도) (모든 속성 계산)
    ai_count = 0
    brush_count = 0
    image_count = 0
    face_count = 0

    for fid in recent_filter_ids:
        meta = get_sticker_meta(fid)  # 캐시에서 4가지 속성을 모두 가져옴
        if meta["has_ai_sticker"]:
            ai_count += 1
        if meta["has_brush_sticker"]:
            brush_count += 1
        if meta["has_image_sticker"]:
            image_count += 1
        if meta["has_face_placement"]:
            face_count += 1

    total = len(recent_filter_ids)
    sticker_profile = {
        "pref_ai": ai_count / total if total > 0 else 0.0,
        "pref_brush": brush_count / total if total > 0 else 0.0,
        "pref_image": image_count / total if total > 0 else 0.0,
        "pref_face": face_count / total if total > 0 else 0.0,
    }
    # --- [수정 끝] ---

    return {"visual_profile": visual_profile, "sticker_profile": sticker_profile}


# --- 4. 핵심 로직: Re-Ranking ---


def re_rank_filters(
    candidates: List[Dict], sticker_profile: Dict, weights: Dict
) -> List[int]:
    ranked_list = []
    for cand in candidates:
        fid = int(cand["id"])
        meta = get_sticker_meta(fid)

        visual_score = 1.0 - cand["distance"]  # 유사도
        popularity_score = get_popularity_score(fid)

        sticker_boost = 0.0
        if meta["has_ai_sticker"]:
            sticker_boost += sticker_profile.get("pref_ai", 0.0)
        if meta["has_face_placement"]:
            sticker_boost += sticker_profile.get("pref_face", 0.0)

        # (참고: 향후 이 부분에 pref_brush, pref_image 로직 확장 가능)
        # if meta["has_brush_sticker"]:
        #     sticker_boost += sticker_profile.get("pref_brush", 0.0)

        final_score = (
            (weights["visual"] * visual_score)
            + (weights["pop"] * popularity_score)
            + (weights["sticker"] * sticker_boost)
        )

        ranked_list.append((final_score, fid))

    ranked_list.sort(key=lambda x: x[0], reverse=True)

    return [fid for score, fid in ranked_list]
