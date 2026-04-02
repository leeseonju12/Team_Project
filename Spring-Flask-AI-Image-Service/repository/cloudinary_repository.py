import io
import cloudinary.uploader
from config.settings import logger

class CloudinaryRepository:
    def upload(self, image_bytes: bytes, folder: str = "ai-generated") -> str:
        try:
            image_stream = io.BytesIO(image_bytes)
            result = cloudinary.uploader.upload(image_stream, folder=folder)
            return result.get("secure_url")
        except Exception as e:
            logger.error(f"Cloudinary Repository Error: {e}")
            return None