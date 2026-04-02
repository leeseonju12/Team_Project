import unittest
from exception.exceptions import AIProxyException

# 1. 테스트를 위한 Mock Settings 설정 (실제 settings와 동일한 구조)
class MockSettings:
    STYLE_TEMPLATES = {
        "카페/베이커리": "Warm natural window lighting, f/1.8 bokeh, 35mm lens...",
        "음식점/식당": "Professional food photography, 45-degree angle...",
        "기타": "Clean minimalist commercial photography, neutral background..."
    }

settings = MockSettings()

# 2. 검증 및 템플릿 반환 로직
def get_style_template(category):
    # 유효성 검사 (앞서 작성한 로직)
    if not category or not str(category).strip():
        raise AIProxyException(
            message="카테고리 정보가 누락되었습니다.",
            status_code=400,
            step="VALIDATION_ERROR"
        )

    if category not in settings.STYLE_TEMPLATES:
        raise AIProxyException(
            message=f"'{category}'은(는) 지원하지 않는 업종 카테고리입니다.",
            status_code=400,
            step="INVALID_CATEGORY"
        )
    
    # 해당 카테고리의 스타일 템플릿 문자열 반환
    return settings.STYLE_TEMPLATES[category]

# 3. 테스트 케이스
class TestTemplateOutput(unittest.TestCase):

    def test_print_cafe_template(self):
        """카페/베이커리 입력 시 올바른 템플릿이 출력되는지 확인"""
        category = "카페/베이커리"
        template = get_style_template(category)
        print(f"\n[입력: {category}] -> 출력: {template}")
        self.assertEqual(template, settings.STYLE_TEMPLATES["카페/베이커리"])

    def test_print_etc_template(self):
        """기타 입력 시 올바른 템플릿이 출력되는지 확인"""
        category = "기타"
        template = get_style_template(category)
        print(f"[입력: {category}] -> 출력: {template}")
        self.assertEqual(template, settings.STYLE_TEMPLATES["기타"])

    def test_invalid_category_error(self):
        """잘못된 카테고리 입력 시 에러 메시지 확인"""
        with self.assertRaises(AIProxyException) as cm:
            get_style_template("없는카테고리")
        print(f"[입력: 없는카테고리] -> 예상된 에러 발생: {cm.exception.message}")

if __name__ == "__main__":
    unittest.main()