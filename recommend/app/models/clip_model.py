# app/models/clip_model.py
from sentence_transformers import SentenceTransformer  # huggingface의 CLIP 모델 사용


class ClipModel:
    def __init__(self):
        self.model = SentenceTransformer("clip-ViT-B-32")

    def encode_text(self, text_list):
        return self.model.encode(text_list, normalize_embeddings=True)

    # 문자열을 벡터로 변환 (정규화 포함)
    def encode_image(self, image_features):
        return self.model.encode(image_features, normalize_embeddings=True)


# 나중에 EC2 GPU 서버로 옮기면, device='cuda' 옵션을 추가해 가속 가능.
