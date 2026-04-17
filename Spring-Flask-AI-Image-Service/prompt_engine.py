"""
prompt_engine.py
────────────────────────────────────────────────────────────────────────────────
마케팅 자동화 플랫폼 – AI 이미지 프롬프트 생성 엔진 (4단계 파이프라인)

[파이프라인 4단계]
  Step 1  Core Object      – 핵심 피사체 강제 가중치 (Force Weighting)
  Step 2  Industry Context – 업종 DB 기반 상황 자동 생성
  Step 3  Style Template   – 업종 기반 시각 속성 결합
  Step 4  Quality Booster  – 상업용 기술 키워드 자동 추가 + 임계치 필터
────────────────────────────────────────────────────────────────────────────────
"""

from __future__ import annotations
import re
import logging
from dataclasses import dataclass, field
from enum import Enum

logger = logging.getLogger(__name__)


class IndustryType(str, Enum):
    CAFE_BAKERY = "카페/베이커리"
    RESTAURANT = "음식점/식당"
    BEAUTY = "미용/뷰티"
    FASHION = "패션/의류"
    LODGING = "숙박/펜션"
    FITNESS_SPORTS = "피트니스/스포츠"
    EDUCATION = "교육/학원"
    MEDICAL = "의료/병원"
    RETAIL_SHOPPING = "소매/쇼핑"
    ETC = "기타"

    @classmethod
    def from_value(cls, value: str | "IndustryType") -> "IndustryType":
        if isinstance(value, cls):
            return value

        normalized = str(value).strip()
        for industry_type in cls:
            if industry_type.value == normalized:
                return industry_type

        raise ValueError(
            f"'{value}'은(는) 지원하지 않는 업종입니다. "
            f"지원 업종: {[item.value for item in cls]}"
        )


NEGATIVE_PROMPT = (
    "blurry, low quality, pixelated, watermark, text, logo, "
    "distorted, ugly, bad anatomy, extra limbs, deformed hands, "
    "overexposed, underexposed, noise, grain, amateur, cartoon, "
    "nsfw, violence, disturbing content"
)


@dataclass(frozen=True)
class IndustryProfile:
    style_template: str
    style_focus: str
    scene: str
    lighting: str
    lens_style: str


# ══════════════════════════════════════════════════════════════════════════════
# 1. 업종별 프롬프트 프로파일 (Single Source)
#    각 업종은 5가지 속성을 갖습니다.
#      - style_template : 기존 긴 스타일 원문 (참고/호환용)
#      - style_focus    : 최종 프롬프트에 넣는 압축 스타일 핵심
#      - scene      : 피사체가 놓일 배경/상황 문장 (Step 2)
#      - lighting   : 최적 조명 조건              (Step 3 일부)
#      - lens_style : 권장 렌즈/구도              (Step 3 일부)
# ══════════════════════════════════════════════════════════════════════════════

INDUSTRY_PROFILES: dict[IndustryType, IndustryProfile] = {
    IndustryType.CAFE_BAKERY: IndustryProfile(
        style_template=(
            "Warm natural window lighting, f/1.8 bokeh, 35mm lens, cozy cafe interior, "
            "focus on food texture, steam, latte art, wooden table, muted earth tones, "
            "Instagrammable aesthetic, soft shadows."
        ),
        style_focus=(
            "food texture, steam, latte art, wooden table, muted earth tones, "
            "Instagrammable cafe aesthetic"
        ),
        scene=(
            "on a rustic marble table inside a bright cozy cafe, "
            "surrounded by coffee beans and a latte art cup"
        ),
        lighting="soft warm window natural light, golden hour tone",
        lens_style="50mm macro lens, f/1.8 shallow depth-of-field, bokeh background",
    ),
    IndustryType.RESTAURANT: IndustryProfile(
        style_template=(
            "Professional food photography, 45-degree angle, bright natural side lighting, "
            "steam rising, vibrant fresh ingredients, elegant plating, "
            "clean marble or light wood surface, appetizing and commercial grade."
        ),
        style_focus=(
            "fresh ingredients, elegant plating, clean marble or light wood surface, "
            "appetizing commercial food styling"
        ),
        scene=(
            "elegantly plated on a white ceramic dish at a fine dining table, "
            "steam gently rising, garnished with fresh herbs"
        ),
        lighting="bright natural side lighting, subtle fill light",
        lens_style="45-degree overhead angle, 35mm lens, sharp food detail",
    ),
    IndustryType.BEAUTY: IndustryProfile(
    style_template=(
        "clean premium beauty aesthetic, soft diffused or natural lighting, "
        "balanced exposure, refined texture detail, professional and trustworthy mood"
    ),
    style_focus=(
        "subject-focused composition, clean environment, natural skin and hair detail"
    ),
    scene=(
        "in a modern beauty space such as salon, clinic, studio, or cosmetic shop, "
        "featuring product, service, or result-focused composition"
    ),
    lighting="soft diffused or clean indoor lighting",
    lens_style="macro, portrait, or wide lens depending on subject",
    ),
    IndustryType.FASHION: IndustryProfile(
        style_template=(
            "Editorial fashion photography, natural daylight, sophisticated urban or studio background, "
            "sharp focus on fabric texture and stitching, balanced color saturation, "
            "dynamic composition, high-end boutique aesthetic."
        ),
        style_focus=(
            "sophisticated boutique setting, sharp fabric texture and stitching, "
            "balanced color saturation, editorial composition"
        ),
        scene=(
            "displayed on a neutral linen surface in a modern boutique, "
            "with carefully folded fabric texture visible"
        ),
        lighting="natural daylight from a side window, slight shadow for depth",
        lens_style="35mm editorial lens, sharp fabric stitching detail",
    ),
    IndustryType.LODGING: IndustryProfile(
        style_template=(
            "Architectural interior photography, straight vertical lines, golden hour light, "
            "airy and spacious feel, crisp white bedding, Scandinavian minimalism, "
            "peaceful resort ambiance, 8k resolution, high dynamic range."
        ),
        style_focus=(
            "crisp white bedding, Scandinavian minimalism, peaceful resort ambiance, "
            "airy spacious interior"
        ),
        scene=(
            "set in a serene hotel room with crisp white bedding, "
            "large window overlooking a peaceful landscape"
        ),
        lighting="golden hour sunlight streaming through sheer curtains",
        lens_style="wide-angle architectural lens, straight vertical lines, airy composition",
    ),
    IndustryType.FITNESS_SPORTS: IndustryProfile(
        style_template=(
            "Hard dramatic lighting, rim lighting to define muscle and shape, "
            "high contrast, vibrant energetic palette, gym or stadium background, "
            "grit and determination atmosphere, sharp motion freeze."
        ),
        style_focus=(
            "gym or stadium background, energetic palette, grit and determination, "
            "sharp motion freeze"
        ),
        scene=(
            "positioned on a polished gym floor surrounded by equipment, "
            "high-energy athletic atmosphere"
        ),
        lighting="hard dramatic rim lighting, high contrast, vivid shadows",
        lens_style="24mm wide lens, motion-freeze shutter, dynamic diagonal composition",
    ),
    IndustryType.EDUCATION: IndustryProfile(
        style_template=(
            "Soft bright daylight, clean organized classroom, modern stationery, "
            "shallow depth of field, turquoise and white accents, "
            "inspirational and scholarly atmosphere, minimalist academic look."
        ),
        style_focus="turquoise and white accents, minimalist academic look",
        scene=(
            "placed on a clean wooden desk in a modern classroom, "
            "with notebooks and stationery neatly arranged"
        ),
        lighting="brightly lit by soft cool daylight, controlled exposure, turquoise and white color palette",
        lens_style="35mm lens, shallow DOF, inspirational scholarly atmosphere",
    ),
    IndustryType.MEDICAL: IndustryProfile(
        style_template=(
            "Clean bright studio lighting, sterile but welcoming atmosphere, "
            "professional medical equipment, soft teal and white color palette, "
            "hyper-realistic, trustworthy and high-tech healthcare vibe."
        ),
        style_focus=(
            "sterile but welcoming atmosphere, professional medical equipment, "
            "soft teal and white palette, healthcare brand credibility"
        ),
        scene=(
            "displayed in a sterile yet welcoming clinic interior, "
            "next to professional medical equipment on a teal surface"
        ),
        lighting="clean bright studio lighting, hygienic blue-white tones",
        lens_style="wide clinical lens, hyper-realistic detail",
    ),
    IndustryType.RETAIL_SHOPPING: IndustryProfile(
        style_template=(
            "Commercial product shot, soft box lighting, 3-point setup, "
            "sharp focus from edge to edge, pure white background, "
            "accurate color representation, zero distortion, e-commerce ready."
        ),
        style_focus=(
            "pure white background, accurate color representation, zero distortion, "
            "e-commerce ready product presentation"
        ),
        scene=(
            "centered on a pure white product photography surface, "
            "clean and distraction-free retail context"
        ),
        lighting="3-point softbox studio lighting, zero hard shadows",
        lens_style="flat lay 90-degree angle, sharp edge-to-edge focus, e-commerce ready",
    ),
    IndustryType.ETC: IndustryProfile(
        style_template=(
            "Clean minimalist commercial photography, neutral background, "
            "balanced natural lighting, sharp focus, high-quality material texture, "
            "versatile professional aesthetic, 8k."
        ),
        style_focus=(
            "neutral background, high-quality material texture, versatile professional aesthetic"
        ),
        scene=(
            "placed on a neutral minimalist surface in a professional studio setting"
        ),
        lighting="balanced natural lighting, soft fill light",
        lens_style="50mm standard lens, clean commercial aesthetic",
    ),
}

SUPPORTED_CATEGORIES = tuple(industry_type.value for industry_type in IndustryType)


# ══════════════════════════════════════════════════════════════════════════════
# 2. Quality Booster – 상업용 기술 키워드 세트
#    Step 4에서 자동으로 추가됩니다.
#    임계치 로직에 의해 필요 시 일부가 제거됩니다.
# ══════════════════════════════════════════════════════════════════════════════

QUALITY_BOOSTERS: list[str] = [
    "commercial photography",
    "award-winning composition",
    "intricate detail",
    "professional color grading",
    "photorealistic",
]

PROMPT_TOKEN_THRESHOLD = 100
BOOSTER_REDUCE_STEP = 2


# ══════════════════════════════════════════════════════════════════════════════
# 3. PromptBuildRequest – 입력 DTO
# ══════════════════════════════════════════════════════════════════════════════

@dataclass
class PromptBuildRequest:
    """
    프롬프트 생성 엔진에 전달되는 입력값.

    Attributes:
        product_name   : 핵심 상품/메뉴명 (예: "아이스 아메리카노")
                         → 기존 ImageGenerateRequest.keyword에 해당
        industry_type  : 업종 카테고리 (예: "카페/베이커리")
                         → 기존 ImageGenerateRequest.category에 해당
        description    : 추가 설명 번역문 (선택)
                         → 기존 ImageGenerateRequest.description에 해당
    """
    product_name: str
    industry_type: IndustryType | str
    description: str | None = None

    def __post_init__(self):
        self.product_name = self.product_name.strip()
        self.description = (self.description or "").strip() or None

        if not self.product_name:
            raise ValueError("product_name은 비어 있을 수 없습니다.")

        self.industry_type = IndustryType.from_value(self.industry_type)


# ══════════════════════════════════════════════════════════════════════════════
# 4. PromptBuildResult – 출력 DTO
# ══════════════════════════════════════════════════════════════════════════════

@dataclass
class PromptBuildResult:
    """
    프롬프트 엔진의 최종 출력물.

    Attributes:
        final_prompt    : Stability AI에 전달할 완성된 영문 프롬프트
        debug_segments  : 각 단계별 세그먼트 (디버깅/로깅 용도)
        token_count     : 최종 프롬프트의 토큰(단어) 수
        booster_trimmed : 임계치 초과로 제거된 Quality Booster 개수
    """
    final_prompt: str
    debug_segments: dict = field(default_factory=dict)
    token_count: int = 0
    booster_trimmed: int = 0


# ══════════════════════════════════════════════════════════════════════════════
# 5. PromptEngine – 4단계 파이프라인 실행 클래스
# ══════════════════════════════════════════════════════════════════════════════

class PromptEngine:
    """
    4단계 프롬프트 빌드 파이프라인을 실행합니다.

    사용 예:
        engine = PromptEngine()
        req = PromptBuildRequest(
            product_name="아이스 아메리카노",
            industry_type="카페/베이커리",
        )
        result = engine.build(req)
        print(result.final_prompt)
    """

    # ── Step 1: Core Object (Force Weighting) ──────────────────────────────
    def _build_core_object(self, req: PromptBuildRequest) -> str:
        """
        핵심 피사체를 프롬프트의 맨 앞에 강하게 고정합니다.
        Stability AI는 프롬프트 앞부분에 위치한 토큰에 더 높은 가중치를 부여하므로,
        상품명이 배경 묘사에 묻히지 않도록 subject 앞에 배치합니다.

        [Force Weighting 전략]
        - 핵심 명사를 영문으로 변환한 뒤 "a close-up of [product]"로 감싸
          Stability의 attention 메커니즘이 피사체에 집중하게 유도합니다.
        """
        product = req.product_name
        core = f"a close-up of {product}"
        if req.description:
            core = f"{core}, featuring {req.description}"

        logger.debug(f"[Step 1 Core Object] {core}")
        return core

    # ── Step 2: Industry Context (자동 상황 생성) ─────────────────────────
    def _build_industry_context(self, req: PromptBuildRequest) -> str:
        """
        업종 DB(INDUSTRY_PROFILES)에서 해당 업종의 scene 컨텍스트를 꺼냅니다.
        유저에게 "어떤 상황에서 찍고 싶으세요?"를 묻지 않아도
        업종 정보만으로 적절한 배경을 자동 삽입합니다.
        """
        profile = INDUSTRY_PROFILES[req.industry_type]
        context = profile.scene
        logger.debug(f"[Step 2 Industry Context] {context}")
        return context

    # ── Step 3: Style Template (조명 + 렌즈) ──────────────────────────────
    def _build_style_template(self, req: PromptBuildRequest) -> str:
        """
        단일 INDUSTRY_PROFILES에서 카테고리별 핵심 미감, 조명, 렌즈 스타일을
        꺼내어 겹치지 않게 결합합니다.
        """
        profile = INDUSTRY_PROFILES[req.industry_type]
        lighting = profile.lighting
        lens_style = profile.lens_style
        style_focus = profile.style_focus

        style = f"{style_focus}, {lighting}, {lens_style}"
        logger.debug(f"[Step 3 Style Template] {style}")
        return style

    # ── Step 4: Quality Booster + 임계치 필터 ─────────────────────────────
    def _apply_quality_boosters(self, segments: list[str]) -> tuple[str, int]:
        """
        상업용 사진 품질을 높이는 기술 키워드를 자동으로 추가합니다.
        """
        current_text = ", ".join(segments)
        current_token_count = _count_tokens(current_text)
        remaining_budget = PROMPT_TOKEN_THRESHOLD - current_token_count

        selected_boosters: list[str] = []
        trimmed_count = 0

        for booster in QUALITY_BOOSTERS:
            booster_tokens = _count_tokens(booster)
            if remaining_budget >= booster_tokens:
                selected_boosters.append(booster)
                remaining_budget -= booster_tokens
            else:
                trimmed_count += 1
                logger.info(
                    f"[Step 4 Threshold] 토큰 초과로 부스터 제거: '{booster}' "
                    f"(잔여 예산 {remaining_budget} < 필요 {booster_tokens})"
                )

        booster_segment = ", ".join(selected_boosters)
        logger.debug(f"[Step 4 Quality Booster] {booster_segment}")
        return booster_segment, trimmed_count

    # ── 메인 빌드 함수 ────────────────────────────────────────────────────
    def build(self, req: PromptBuildRequest) -> PromptBuildResult:
        """
        4단계 파이프라인을 순서대로 실행하여 최종 프롬프트를 반환합니다.
        """
        seg_core = self._build_core_object(req)
        seg_context = self._build_industry_context(req)
        seg_style = self._build_style_template(req)

        segments = [seg_core, seg_context, seg_style]

        seg_booster, trimmed = self._apply_quality_boosters(segments)
        if seg_booster:
            segments.append(seg_booster)

        final_prompt = ", ".join(segments)
        token_count = _count_tokens(final_prompt)

        logger.info(
            f"[PromptEngine] 최종 프롬프트 생성 완료 "
            f"(토큰: {token_count}, 부스터 제거: {trimmed})\n"
            f"  → {final_prompt}"
        )

        return PromptBuildResult(
            final_prompt=final_prompt,
            debug_segments={
                "step1_core": seg_core,
                "step2_context": seg_context,
                "step3_style": seg_style,
                "step4_booster": seg_booster,
            },
            token_count=token_count,
            booster_trimmed=trimmed,
        )


# ══════════════════════════════════════════════════════════════════════════════
# 6. 내부 유틸리티 함수
# ══════════════════════════════════════════════════════════════════════════════

def _count_tokens(text: str) -> int:
    """
    영문 기준 공백 분리 토큰 수를 반환합니다 (근사치).
    Stability AI는 정확한 BPE 토크나이저를 쓰지만,
    단어 기준 카운팅이 실용적인 근사 임계치로 충분합니다.
    """
    return len(re.findall(r"\S+", text))
