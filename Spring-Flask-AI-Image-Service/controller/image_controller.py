"""
controller/image_controller.py
────────────────────────────────────────────────────────────────────────────────
mood, search_trend 관련 요소를 제거한 버전입니다.
기존 /api/ai/generate 엔드포인트는 category, keyword만 받습니다.
────────────────────────────────────────────────────────────────────────────────
"""

import logging

from flask import Blueprint, request, jsonify
from service.image_service import ImageService
from dto.image_dto import ImageGenerateRequest, ImageGenerateResponse
from exception.exceptions import AIProxyException
from prompt_engine import SUPPORTED_CATEGORIES

image_bp = Blueprint('image', __name__)
image_service = ImageService()
logger = logging.getLogger(__name__)


DESCRIPTION_FIELD_NAMES = (
    "description",
    "additionalDescription",
    "additional_description",
    "detailDescription",
    "imageDescription",
    "promptDescription",
    "details",
    "detail",
    "content",
)


def _get_optional_text(data: dict, field_names: tuple[str, ...]) -> str | None:
    for field_name in field_names:
        value = data.get(field_name)
        if value and str(value).strip():
            return str(value).strip()
    return None


@image_bp.route("/generate", methods=["POST"])
def generate():
    """
    Request Body (JSON):
    {
        "category": "카페/베이커리",
        "keyword": "아이스 아메리카노",
        "description": "유리잔에 얼음이 담기고 표면에 물방울이 맺힌 느낌"
    }
    """
    try:
        data = request.get_json() or {}
        category = data.get("category")
        keyword = data.get("keyword")
        description = _get_optional_text(data, DESCRIPTION_FIELD_NAMES)
        logger.info("[STS request] keys=%s", list(data.keys()))
        logger.info("[STS request] raw=%s", data)
        logger.info("[STS request] category=%s", category)
        logger.info("[STS request] keyword=%s", keyword)
        logger.info("[STS request] description=%s", description)

        # 필수 필드 검증
        if not category or not str(category).strip():
            raise AIProxyException(
                message="카테고리 정보가 누락되었습니다.",
                status_code=400,
                step="VALIDATION_ERROR"
            )

        if category not in SUPPORTED_CATEGORIES:
            raise AIProxyException(
                message=f"'{category}'은(는) 지원하지 않는 업종 카테고리입니다.",
                status_code=400,
                step="INVALID_CATEGORY"
            )

        if not keyword or not str(keyword).strip():
            raise AIProxyException(
                message="이미지 생성을 위한 키워드가 누락되었습니다.",
                status_code=400,
                step="VALIDATION_ERROR"
            )

        # DTO 생성 및 서비스 호출
        req_dto = ImageGenerateRequest(
            keyword=keyword,
            category=category,
            description=description
        )

        image_url, refined_prompt = image_service.process_pipeline(
            keyword=req_dto.keyword,
            category=req_dto.category,
            description=req_dto.description,
        )

        res_dto = ImageGenerateResponse(
            imageUrl=image_url,
            refinedPrompt=refined_prompt
        )
        return jsonify(res_dto.to_dict()), 200

    except AIProxyException as e:
        return jsonify({
            "error": e.message,
            "step": e.step
        }), e.status_code

    except Exception as e:
        return jsonify({
            "error": str(e),
            "step": "INTERNAL_SERVER_ERROR"
        }), 500
