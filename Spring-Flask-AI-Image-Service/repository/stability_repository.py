import requests
from config.settings import Config, logger
from dto.image_dto import StabilityImageRequest

class StabilityRepository:
    def fetch_image(self, request_dto: StabilityImageRequest) -> bytes:
        headers = {
            "Authorization": f"Bearer {Config.STABILITY_API_KEY}",
            "Accept": "image/*"
        }
        files = request_dto.to_multipart_files()
        
        response = requests.post(Config.STABILITY_API_URL, headers=headers, files=files, timeout=60)
        if response.status_code != 200:
            logger.error(f"Stability API Error: {response.text}")
            return None
        return response.content
