import os
import logging
import cloudinary

# 로깅 설정
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s - %(message)s")
logger = logging.getLogger(__name__)

from dotenv import load_dotenv

load_dotenv()

class Config:
    STABILITY_API_KEY = os.environ.get("STABILITY_API_KEY")
    STABILITY_API_URL = "https://api.stability.ai/v2beta/stable-image/generate/core"
    
    # Cloudinary 설정
    CLOUDINARY_CLOUD_NAME = os.environ.get("CLOUDINARY_CLOUD_NAME")
    CLOUDINARY_API_KEY = os.environ.get("CLOUDINARY_API_KEY")
    CLOUDINARY_API_SECRET = os.environ.get("CLOUDINARY_API_SECRET")

    @staticmethod
    def init_cloudinary():
        cloudinary.config(
            cloud_name=Config.CLOUDINARY_CLOUD_NAME,
            api_key=Config.CLOUDINARY_API_KEY,
            api_secret=Config.CLOUDINARY_API_SECRET,
            secure=True
        )

# 스타일 템플릿 및 상수
STYLE_TEMPLATES = {
    "카페/베이커리": (
        "Warm natural window lighting, f/1.8 bokeh, 35mm lens, cozy cafe interior, "
        "focus on food texture, steam, latte art, wooden table, muted earth tones, "
        "Instagrammable aesthetic, soft shadows."
    ),
    "음식점/식당": (
        "Professional food photography, 45-degree angle, bright natural side lighting, "
        "steam rising, vibrant fresh ingredients, elegant plating, "
        "clean marble or light wood surface, appetizing and commercial grade."
    ),
    "미용/뷰티": (
        "High-key soft diffused lighting, clean pastel background, macro product detail, "
        "glass-like skin reflection, minimalist luxury, harmonious palette, "
        "dewy and luminous finish, high-end cosmetic brand vibe."
    ),
    "패션/의류": (
        "Editorial fashion photography, natural daylight, sophisticated urban or studio background, "
        "sharp focus on fabric texture and stitching, balanced color saturation, "
        "dynamic composition, high-end boutique aesthetic."
    ),
    "숙박/펜션": (
        "Architectural interior photography, straight vertical lines, golden hour light, "
        "airy and spacious feel, crisp white bedding, Scandinavian minimalism, "
        "peaceful resort ambiance, 8k resolution, high dynamic range."
    ),
    "피트니스/스포츠": (
        "Hard dramatic lighting, rim lighting to define muscle and shape, "
        "high contrast, vibrant energetic palette, gym or stadium background, "
        "grit and determination atmosphere, sharp motion freeze."
    ),
    "교육/학원": (
        "Soft bright daylight, clean organized classroom, modern stationery, "
        "shallow depth of field, turquoise and white accents, "
        "inspirational and scholarly atmosphere, minimalist academic look."
    ),
    "의료/병원": (
        "Clean bright studio lighting, sterile but welcoming atmosphere, "
        "professional medical equipment, soft teal and white color palette, "
        "hyper-realistic, trustworthy and high-tech healthcare vibe."
    ),
    "소매/쇼핑": (
        "Commercial product shot, soft box lighting, 3-point setup, "
        "sharp focus from edge to edge, pure white background, "
        "accurate color representation, zero distortion, e-commerce ready."
    ),
    "기타": (
        "Clean minimalist commercial photography, neutral background, "
        "balanced natural lighting, sharp focus, high-quality material texture, "
        "versatile professional aesthetic, 8k."
    ),
}

# =============================================================================
# Negative Prompt (공통)
# 설계 의도:
#   Negative Prompt는 "생성하지 말아야 할 요소"를 명시하여 이미지 품질을 높임.
#   상업용 이미지에서 흔히 나타나는 결함들(흐릿함, 노이즈, AI 특유의 어색한
#   손 묘사, 워터마크 등)을 명시적으로 제외시켜 재생성 비용을 절감함.
# =============================================================================
NEGATIVE_PROMPT = (
    "blurry, low quality, pixelated, watermark, text, logo, "
    "distorted, ugly, bad anatomy, extra limbs, deformed hands, "
    "overexposed, underexposed, noise, grain, amateur, cartoon, "
    "nsfw, violence, disturbing content"
)