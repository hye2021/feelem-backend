import os
import uuid
import boto3
import torch
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from diffusers import StableDiffusionPipeline
from transformers import MarianMTModel, MarianTokenizer
from rembg import remove
from PIL import Image

# ----------------------------
# 환경 변수 및 설정
# ----------------------------
HUGGINGFACE_TOKEN = os.getenv("HUGGINGFACE_TOKEN")
S3_BUCKET = "feelem-s3-bucket"
REGION_NAME = "ap-northeast-2"

# ----------------------------
# S3 클라이언트 초기화
# ----------------------------
try:
    s3_client = boto3.client("s3", region_name=REGION_NAME)
    print("✅ S3 클라이언트 초기화 완료")
except Exception as e:
    print(f"❌ S3 클라이언트 초기화 실패: {e}")

# ----------------------------
# 번역 모델 (Ko → En)
# ----------------------------
print("🌀 Loading translation model...")
translator_model = "Helsinki-NLP/opus-mt-ko-en"
tokenizer = MarianTokenizer.from_pretrained(translator_model)
model = MarianMTModel.from_pretrained(translator_model)


def translate_korean_to_english(text: str) -> str:
    inputs = tokenizer(text, return_tensors="pt", padding=True)
    translated = model.generate(**inputs)
    return tokenizer.batch_decode(translated, skip_special_tokens=True)[0]


# ----------------------------
# Stable Diffusion v1.5 파이프라인
# ----------------------------
print("🧠 Loading Stable Diffusion v1.5 model... (first time only)")
pipe = StableDiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5",
    torch_dtype=torch.float16,
    use_auth_token=HUGGINGFACE_TOKEN,
).to("cuda" if torch.cuda.is_available() else "cpu")

# g4dn(T4 GPU) 환경 메모리 최적화
pipe.enable_attention_slicing()
pipe.enable_vae_slicing()

# ----------------------------
# FastAPI 서버 초기화
# ----------------------------
app = FastAPI()


class PromptRequest(BaseModel):
    prompt_ko: str


@app.get("/")
async def root():
    return {"status": "ok", "device": "cuda" if torch.cuda.is_available() else "cpu"}


# ----------------------------
# 이미지 생성 & 업로드
# ----------------------------
@app.post("/gensticker")
async def generate_sticker(request: PromptRequest):
    try:
        prompt_ko = request.prompt_ko
        print(f"🎨 프롬프트 수신: {prompt_ko}")

        # 1️ 번역
        prompt_en = translate_korean_to_english(prompt_ko)
        print(f"🔤 영어 번역: {prompt_en}")

        # 2️ 이미지 생성
        image = pipe(prompt_en).images[0]

        # 3️ 배경 제거
        image_no_bg = remove(image)

        # 4️ 임시 파일로 저장
        tmp_path = f"/tmp/{uuid.uuid4()}.png"
        image_no_bg.save(tmp_path, "PNG")

        # 5️ S3 업로드
        s3_key = f"stickers/{os.path.basename(tmp_path)}"
        s3_client.upload_file(
            tmp_path,
            S3_BUCKET,
            s3_key,
            ExtraArgs={"ContentType": "image/png"},
        )

        # 6️ S3 URL 생성
        image_url = f"https://{S3_BUCKET}.s3.{REGION_NAME}.amazonaws.com/{s3_key}"
        print(f"✅ 업로드 완료: {image_url}")

        return JSONResponse(
            content={"prompt_ko": prompt_ko, "prompt_en": prompt_en, "url": image_url}
        )

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        return JSONResponse(content={"error": str(e)}, status_code=500)
