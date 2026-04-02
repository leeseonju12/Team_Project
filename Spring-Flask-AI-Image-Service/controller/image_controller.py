from flask import Blueprint, request, jsonify
from config import settings
from service.image_service import ImageService
from dto.image_dto import ImageGenerateRequest, ImageGenerateResponse
from exception.exceptions import AIProxyException

image_bp = Blueprint('image', __name__)
image_service = ImageService()

@image_bp.route("/generate", methods=["POST"])
def generate():
    try:
        data = request.get_json()
        category = data.get('category')
        keyword = data.get('keyword')

        # 1. 값이 아예 없거나(None), 빈 문자열인 경우 검증
        if not category or not str(category).strip():
            raise AIProxyException(
                message="카테고리 정보가 누락되었습니다.",
                status_code=400,
                step="VALIDATION_ERROR"
            )

        # 2. settings에 정의된 스타일 템플릿 키값에 존재하는지 확인
        # settings.STYLE_TEMPLATES는 딕셔너리 형태여야 합니다.
        if category not in settings.STYLE_TEMPLATES:
            raise AIProxyException(
                message=f"'{category}'은(는) 지원하지 않는 업종 카테고리입니다.",
                status_code=400,
                step="INVALID_CATEGORY"
            )

        # 3. 키워드 누락 여부도 함께 체크 (권장)
        if not keyword or not str(keyword).strip():
            raise AIProxyException(
                message="이미지 생성을 위한 키워드가 누락되었습니다.",
                status_code=400,
                step="VALIDATION_ERROR"
            )

        # DTO 생성 및 서비스 호출
        req_dto = ImageGenerateRequest(keyword=keyword, category=category)
        image_url, refined_prompt = image_service.process_pipeline(req_dto.keyword, req_dto.category)
        
        res_dto = ImageGenerateResponse(imageUrl=image_url, refinedPrompt=refined_prompt)
        return jsonify(res_dto.to_dict()), 200

    except AIProxyException as e:
        return jsonify({"error": e.message, "step": e.step}), e.status_code
    except Exception as e:
        return jsonify({"error": str(e), "step": "INTERNAL_SERVER_ERROR"}), 500