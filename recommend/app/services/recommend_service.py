import json
import numpy as np
from app.models.clip_model import ClipModel
from app.utils.similarity import get_top_n

clip = ClipModel()


def get_personalized_recommendations(request):
    user_tags = request["userPreferredTags"]
    user_vector = clip.encode_text([" ".join(user_tags)])[0]

    # 필터 벡터 & 메타데이터 로드
    filter_vectors = np.load("app/data/filter_embeddings.npy")
    with open("app/data/metadata.json", "r", encoding="utf-8") as f:
        metadata = json.load(f)

    # 가중치
    weights = {"a": 0.4, "b": 0.2, "c": 0.2, "d": 0.2}

    top_indices = get_top_n(user_vector, filter_vectors, weights, metadata, n=5)
    return {"recommendedFilterIds": top_indices}
