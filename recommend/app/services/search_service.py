import json
import numpy as np
from app.models.clip_model import ClipModel
from app.utils.similarity import get_top_n

clip = ClipModel()


def get_integrated_search_results(request):
    query = request["query"]
    query_vector = clip.encode_text([query])[0]

    filter_vectors = np.load("app/data/filter_embeddings.npy")
    with open("app/data/metadata.json", "r", encoding="utf-8") as f:
        metadata = json.load(f)

    weights = {"a": 0.4, "b": 0.2, "c": 0.2, "d": 0.2}
    top_indices = get_top_n(query_vector, filter_vectors, weights, metadata, n=5)

    return {"searchResultsFilterIds": top_indices}
