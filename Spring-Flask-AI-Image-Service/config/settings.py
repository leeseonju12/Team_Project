import os
import logging

try:
    import cloudinary
except ImportError:
    cloudinary = None

# 로깅 설정
logging.basicConfig(level=logging.INFO, format="%(name)s - %(message)s")
logger = logging.getLogger(__name__)

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None

if load_dotenv is not None:
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
        if cloudinary is None:
            logger.warning("cloudinary 패키지가 설치되어 있지 않아 Cloudinary 설정을 건너뜁니다.")
            return

        cloudinary.config(
            cloud_name=Config.CLOUDINARY_CLOUD_NAME,
            api_key=Config.CLOUDINARY_API_KEY,
            api_secret=Config.CLOUDINARY_API_SECRET,
            secure=True
        )

