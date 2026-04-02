from repository.translation_repository import TranslationRepository # 추가
from repository.stability_repository import StabilityRepository
from repository.cloudinary_repository import CloudinaryRepository
from exception.exceptions import TranslationError, GenerationError, UploadError
from config.settings import STYLE_TEMPLATES, NEGATIVE_PROMPT, logger

class ImageService:
    def __init__(self):
        self.translation_repo = TranslationRepository() # Google 대신 MyMemory 저장소 사용
        self.stability_repo = StabilityRepository()
        self.cloudinary_repo = CloudinaryRepository()

    def process_pipeline(self, keyword: str, category: str):
        # 1. Translate (MyMemory 사용)
        translated_keyword = self.translation_repo.translate(keyword)
        
        if not translated_keyword:
            raise TranslationError("번역 서비스 호출에 실패했습니다.", "TRANSLATION")
        
        # 2. Build Prompt
        style = STYLE_TEMPLATES.get(category, STYLE_TEMPLATES["기타"])
        final_prompt = f"{translated_keyword}, {style}"
        
        # 3. Generate
        image_bytes = self.stability_repo.fetch_image(final_prompt, NEGATIVE_PROMPT)
        if not image_bytes:
            raise GenerationError("이미지 생성에 실패했습니다.", "GENERATION")
            
        # 4. Upload
        image_url = self.cloudinary_repo.upload(image_bytes)
        if not image_url:
            raise UploadError("이미지 업로드에 실패했습니다.", "UPLOAD")
            
        return image_url, final_prompt