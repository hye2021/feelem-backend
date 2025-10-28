from fastapi import FastAPI, Request
from fastapi.responses import FileResponse
from pydantic import BaseModel
from transformers import MarianMTModel, MarianTokenizer
from diffusers import StableDiffusionPipeline
from rembg import remove
from PIL import Image
import torch, uuid, os

app = FastAPI()

# ------------------------------
# 1. 번역 모델 로드
# ------------------------------
model_name = "Helsinki-NLP/opus-mt-ko-en"
tokenizer = MarianTokenizer.from_pretrained(model_name)
translator = MarianMTModel.from_pretrained(model_name)


def translate_korean_to_english(text: str) -> str:
    inputs = tokenizer(text, return_tensors="pt", padding=True)
    translated = translator.generate(**inputs)
    return tokenizer.batch_decode(translated, skip_special_tokens=True)[0]


# ------------------------------
# 2. Stable Diffusion 파이프라인 로드
# ------------------------------
pipe = StableDiffusionPipeline.from_pretrained(
    "CompVis/stable-diffusion-v1-4",
    torch_dtype=torch.float16,
    revision="fp16",
    use_auth_token=os.getenv("HUGGINGFACE_TOKEN"),
).to("cuda")

pipe.enable_attention_slicing()


# ------------------------------
# 3. 요청 모델
# ------------------------------
class PromptRequest(BaseModel):
    prompt_ko: str


SAVE_DIR = "generated_images"
os.makedirs(SAVE_DIR, exist_ok=True)


# ------------------------------
# 4. API
# ------------------------------
@app.get("/")
async def root():
    return {"status": "ok"}


@app.post("/gensticker")
async def generate_sticker(request: PromptRequest):
    prompt_en = translate_korean_to_english(request.prompt_ko)
    image = pipe(prompt_en).images[0]
    image_no_bg = remove(image)

    file_id = str(uuid.uuid4())
    output_path = os.path.join(SAVE_DIR, f"{file_id}.png")
    image_no_bg.save(output_path)

    return FileResponse(output_path, media_type="image/png", filename="sticker.png")
