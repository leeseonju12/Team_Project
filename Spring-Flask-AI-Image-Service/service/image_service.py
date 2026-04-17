"""
service/image_service.py
"""

from repository.translation_repository import TranslationRepository
from repository.stability_repository import StabilityRepository
from repository.cloudinary_repository import CloudinaryRepository
from exception.exceptions import TranslationError, GenerationError, UploadError
from config.settings import logger
from dto.image_dto import StabilityImageRequest

from prompt_engine import NEGATIVE_PROMPT, PromptEngine, PromptBuildRequest


class ImageService:
    def __init__(self):
        self.translation_repo = TranslationRepository()
        self.stability_repo = StabilityRepository()
        self.cloudinary_repo = CloudinaryRepository()
        self.prompt_engine = PromptEngine()

    def process_pipeline(
        self,
        keyword: str,
        category: str,
        description: str | None = None,
    ) -> tuple[str, str]:
        """
        기존 4단계 파이프라인에 프롬프트 엔진 Step을 삽입한 버전입니다.

        반환값: (image_url, final_prompt)
        컨트롤러(image_controller.py)의 반환 구조는 변경 없습니다.
        """

        # 1. 키워드 번역
        translated_keyword = self.translation_repo.translate(keyword)
        if not translated_keyword:
            raise TranslationError("번역 서비스 호출에 실패했습니다.", "TRANSLATION")

        translated_description = None
        if description:
            logger.info("[Description Translation] source='%s'", description)
            translated_description = self.translation_repo.translate(description)
            if not translated_description:
                raise TranslationError("추가 설명 번역 서비스 호출에 실패했습니다.", "TRANSLATION")
            logger.info("[Description Translation] translated='%s'", translated_description)
        else:
            logger.info("[Description Translation] no description provided")

        # 2. 프롬프트 엔진 실행
        try:
            req = PromptBuildRequest(
                product_name=translated_keyword,
                industry_type=category,
                description=translated_description,
            )
            result = self.prompt_engine.build(req)
            final_prompt = result.final_prompt

            logger.debug(f"[PromptEngine Segments] {result.debug_segments}")
            logger.info(
                f"[PromptEngine] 토큰 수: {result.token_count} | "
                f"부스터 제거: {result.booster_trimmed}"
            )

        except ValueError as e:
            raise GenerationError(str(e), "PROMPT_BUILD")

        # 3. 이미지 생성
        stability_req = StabilityImageRequest(
            prompt=final_prompt,
            negativePrompt=NEGATIVE_PROMPT,
        )
        image_bytes = self.stability_repo.fetch_image(stability_req)
        if not image_bytes:
            raise GenerationError("이미지 생성에 실패했습니다.", "GENERATION")

        # 4. 업로드
        image_url = self.cloudinary_repo.upload(image_bytes)
        if not image_url:
            raise UploadError("이미지 업로드에 실패했습니다.", "UPLOAD")

        return image_url, final_prompt
