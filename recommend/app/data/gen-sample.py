import numpy as np

# 5개의 필터, 각각 512차원짜리 가짜 CLIP 임베딩
np.save("app/data/filter_embeddings.npy", np.random.rand(5, 512))
