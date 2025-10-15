import numpy as np


def cosine_similarity(v1, v2):
    return np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))


def get_top_n(query_vector, all_vectors, weights, metadata, n=5):
    """
    유사도 계산:
    (CLIP 유사도 * a) + (색감 유사도 * b) + (태그 일치도 * c) + (스티커 경향성 * d)
    """
    # CLIP 유사도
    clip_sims = np.dot(all_vectors, query_vector) / (
        np.linalg.norm(all_vectors, axis=1) * np.linalg.norm(query_vector)
    )

    color_sims = np.array([meta["color_similarity"] for meta in metadata])
    tag_sims = np.array([meta["tag_similarity"] for meta in metadata])
    sticker_sims = np.array([meta["sticker_similarity"] for meta in metadata])

    total_sims = (
        clip_sims * weights["a"]
        + color_sims * weights["b"]
        + tag_sims * weights["c"]
        + sticker_sims * weights["d"]
    )

    top_indices = np.argsort(total_sims)[::-1][:n]
    return top_indices.tolist()
