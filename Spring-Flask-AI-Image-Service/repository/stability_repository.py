import requests
from config.settings import Config, logger

class StabilityRepository:
    def fetch_image(self, prompt: str, negative_prompt: str) -> bytes:
        headers = {
            "Authorization": f"Bearer {Config.STABILITY_API_KEY}",
            "Accept": "image/*"
        }
        files = {
            "prompt": (None, prompt),
            "negative_prompt": (None, negative_prompt),
            "aspect_ratio": (None, "1:1"),
            "output_format": (None, "jpeg")
        }
        
        response = requests.post(Config.STABILITY_API_URL, headers=headers, files=files, timeout=60)
        if response.status_code != 200:
            logger.error(f"Stability API Error: {response.text}")
            return None
        return response.content