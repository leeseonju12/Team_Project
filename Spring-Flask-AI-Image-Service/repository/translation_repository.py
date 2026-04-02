import requests
from config.settings import logger

class TranslationRepository:
    def __init__(self):
        # MyMemory 무료 API 엔드포인트
        self.api_url = "https://api.mymemory.translated.net/get"

    def translate(self, text: str, source_lang: str = "ko", target_lang: str = "en") -> str | None:
        """
        MyMemory API를 사용하여 텍스트를 번역합니다.
        무료 버전은 하루 1,000단어 내외의 제한이 있으나 테스트용으로 충분합니다.
        """
        params = {
            "q": text,
            "langpair": f"{source_lang}|{target_lang}"
        }

        try:
            response = requests.get(self.api_url, params=params, timeout=10)
            if response.status_code == 200:
                data = response.json()
                translated_text = data.get("responseData", {}).get("translatedText")
                logger.info(f"MyMemory Translation: '{text}' -> '{translated_text}'")
                return translated_text
            else:
                logger.error(f"MyMemory API Error: {response.status_code}")
                return None
        except Exception as e:
            logger.error(f"MyMemory Network Error: {e}")
            return None