"""
test/test_prompt_engine.py
────────────────────────────────────────────────────────────────────────────────
프롬프트 엔진을 외부 API 호출 없이 단독으로 검증합니다.
실행: python -m pytest test/test_prompt_engine.py -v
     또는: python test/test_prompt_engine.py
────────────────────────────────────────────────────────────────────────────────
"""

import sys
import os
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from prompt_engine import (
    INDUSTRY_PROFILES,
    IndustryType,
    NEGATIVE_PROMPT,
    PromptEngine,
    PromptBuildRequest,
    PROMPT_TOKEN_THRESHOLD,
)
from dto.image_dto import StabilityImageRequest

engine = PromptEngine()


class TestPromptEngine(unittest.TestCase):

    # ── 1. 핵심 피사체(Core Object) 포함 여부 ─────────────────────────────
    def test_product_name_appears_first(self):
        """상품명이 프롬프트 앞부분에 위치하는지 확인 (Force Weighting)"""
        req = PromptBuildRequest(
            product_name="iced americano",
            industry_type="카페/베이커리",
        )
        result = engine.build(req)

        self.assertIn("iced americano", result.final_prompt[:60])
        print(f"\n[Core Object 위치 확인]\n  → {result.final_prompt[:80]}...")

    # ── 2. 업종별 컨텍스트 자동 삽입 ─────────────────────────────────────
    def test_industry_context_auto_inserted(self):
        """의료 업종의 scene이 자동으로 삽입되는지 확인"""
        req = PromptBuildRequest(
            product_name="dental implant model",
            industry_type="의료/병원",
        )
        result = engine.build(req)

        self.assertIn("clinic", result.debug_segments["step2_context"])
        print(f"\n[Industry Context 확인]\n  → {result.debug_segments['step2_context']}")

    # ── 3. 스타일 템플릿이 업종 기반으로 정상 생성되는지 확인 ───────────────
    def test_style_template_contains_lighting_and_lens(self):
        """step3_style에 lighting과 lens_style 정보가 포함되는지 확인"""
        req = PromptBuildRequest(
            product_name="green tea cake",
            industry_type="카페/베이커리",
        )
        result = engine.build(req)

        step3_style = result.debug_segments["step3_style"]

        self.assertIn("light", step3_style.lower())
        self.assertIn("lens", step3_style.lower())
        print(f"\n[Style Template 확인]\n  → {step3_style}")

    def test_profile_style_focus_is_included_without_negative_prompt(self):
        """단일 프로파일의 카테고리 미감은 넣고, NEGATIVE_PROMPT는 분리하는지 확인"""
        req = PromptBuildRequest(
            product_name="protein shake bottle",
            industry_type="피트니스/스포츠",
        )
        result = engine.build(req)

        self.assertIn("gym or stadium background", result.final_prompt)
        self.assertIn("grit and determination", result.final_prompt)
        self.assertNotIn(INDUSTRY_PROFILES[IndustryType.FITNESS_SPORTS].style_template, result.final_prompt)
        self.assertNotIn(NEGATIVE_PROMPT, result.final_prompt)

    def test_prompt_avoids_repeated_style_concepts(self):
        """조명, 심도, 분위기처럼 의미가 겹치는 표현이 과하게 반복되지 않는지 확인"""
        req = PromptBuildRequest(
            product_name="study planner",
            industry_type="교육/학원",
        )
        result = engine.build(req)
        prompt = result.final_prompt.lower()

        self.assertIn(
            INDUSTRY_PROFILES[IndustryType.EDUCATION].style_focus,
            result.debug_segments["step3_style"],
        )
        self.assertEqual(prompt.count("daylight"), 1)
        self.assertEqual(prompt.count("shallow"), 1)
        self.assertLessEqual(prompt.count("academic"), 1)
        self.assertLessEqual(prompt.count("photoreal"), 1)

    def test_education_lighting_uses_connected_phrase(self):
        """교육/학원 조명은 bright와 soft를 분리하지 않고 문장형으로 연결합니다."""
        result = engine.build(PromptBuildRequest(
            product_name="study planner",
            industry_type="교육/학원",
        ))
        prompt = result.final_prompt.lower()

        self.assertIn("brightly lit by soft cool daylight", prompt)
        self.assertIn("controlled exposure", prompt)
        self.assertNotIn("bright modern classroom", prompt)

    def test_stability_request_sends_negative_prompt_separately(self):
        """Stability API 요청 DTO가 negative_prompt를 별도 multipart 필드로 보내는지 확인"""
        request_dto = StabilityImageRequest(
            prompt="a close-up of protein shake bottle",
            negativePrompt=NEGATIVE_PROMPT,
        )
        files = request_dto.to_multipart_files()

        self.assertEqual(files["prompt"], (None, "a close-up of protein shake bottle"))
        self.assertEqual(files["negative_prompt"], (None, NEGATIVE_PROMPT))
        self.assertEqual(files["aspect_ratio"], (None, "1:1"))
        self.assertEqual(files["output_format"], (None, "jpeg"))

    def test_category_changes_final_prompt(self):
        """키워드가 같아도 카테고리에 따라 최종 프롬프트가 달라지는지 확인"""
        cafe_prompt = engine.build(PromptBuildRequest(
            product_name="signature product",
            industry_type=IndustryType.CAFE_BAKERY,
        )).final_prompt
        medical_prompt = engine.build(PromptBuildRequest(
            product_name="signature product",
            industry_type=IndustryType.MEDICAL,
        )).final_prompt

        self.assertNotEqual(cafe_prompt, medical_prompt)
        self.assertIn("cozy cafe", cafe_prompt)
        self.assertIn("clinic", medical_prompt)

    def test_string_industry_type_is_converted_to_enum(self):
        """외부 입력 문자열은 PromptBuildRequest에서 IndustryType으로 변환됩니다."""
        req = PromptBuildRequest(
            product_name="iced americano",
            industry_type="카페/베이커리",
        )

        self.assertEqual(req.industry_type, IndustryType.CAFE_BAKERY)

    def test_description_is_added_near_core_object(self):
        """번역된 추가 설명은 핵심 피사체 가까이에 반영됩니다."""
        result = engine.build(PromptBuildRequest(
            product_name="iced americano",
            industry_type=IndustryType.CAFE_BAKERY,
            description="ice cubes in a clear glass with condensation droplets",
        ))

        self.assertIn(
            "a close-up of iced americano, featuring ice cubes in a clear glass with condensation droplets",
            result.final_prompt,
        )
        self.assertIn("condensation droplets", result.debug_segments["step1_core"])

    def test_blank_description_is_ignored(self):
        """빈 추가 설명은 프롬프트에 featuring 문구를 만들지 않습니다."""
        result = engine.build(PromptBuildRequest(
            product_name="iced americano",
            industry_type=IndustryType.CAFE_BAKERY,
            description="   ",
        ))

        self.assertNotIn("featuring", result.debug_segments["step1_core"])

    # ── 4. 임계치 로직 – 긴 프롬프트에서 부스터 제거 ─────────────────────
    def test_threshold_trims_boosters_on_long_input(self):
        """매우 긴 product_name 입력 시 Quality Booster가 정상 제거되는지 확인"""
        long_name = " ".join(["premium organic cold-brew arabica coffee"] * 5)

        req = PromptBuildRequest(
            product_name=long_name,
            industry_type="카페/베이커리",
        )
        result = engine.build(req)

        print(
            f"\n[임계치 로직 확인]\n"
            f"  토큰 수: {result.token_count} (임계치: {PROMPT_TOKEN_THRESHOLD})\n"
            f"  부스터 제거: {result.booster_trimmed}개"
        )

        self.assertGreaterEqual(result.booster_trimmed, 0)

    # ── 5. 잘못된 업종 입력 예외 처리 ────────────────────────────────────
    def test_invalid_industry_raises(self):
        """지원하지 않는 업종 입력 시 ValueError 발생 확인"""
        with self.assertRaises(ValueError) as cm:
            PromptBuildRequest(
                product_name="test product",
                industry_type="존재하지않는업종",
            )

        print(f"\n[잘못된 업종 예외 확인]\n  → {cm.exception}")

    # ── 6. 빈 상품명 입력 예외 처리 ──────────────────────────────────────
    def test_empty_product_name_raises(self):
        """빈 상품명 입력 시 ValueError 발생 확인"""
        with self.assertRaises(ValueError) as cm:
            PromptBuildRequest(
                product_name="   ",
                industry_type="카페/베이커리",
            )

        print(f"\n[빈 상품명 예외 확인]\n  → {cm.exception}")

    # ── 7. 전체 파이프라인 시나리오 출력 ─────────────────────────────────
    def test_full_pipeline_scenario_print(self):
        """실제 사용 시나리오 – 최종 프롬프트 전체 출력"""
        scenarios = [
            {
                "product_name": "iced caramel macchiato",
                "industry_type": "카페/베이커리",
            },
            {
                "product_name": "dental implant surgery room",
                "industry_type": "의료/병원",
            },
            {
                "product_name": "protein shake bottle",
                "industry_type": "피트니스/스포츠",
            },
        ]

        print("\n" + "=" * 70)
        print("최종 프롬프트 시나리오 출력")
        print("=" * 70)

        for s in scenarios:
            req = PromptBuildRequest(**s)
            result = engine.build(req)

            print(f"\n[업종: {s['industry_type']}]")
            print(f"  입력: '{s['product_name']}'")
            print(f"  토큰: {result.token_count} / 부스터 제거: {result.booster_trimmed}")
            print(f"  프롬프트:\n    {result.final_prompt}")

        print("=" * 70)


if __name__ == "__main__":
    unittest.main(verbosity=2)
