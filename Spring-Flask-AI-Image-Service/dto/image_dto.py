from dataclasses import dataclass, asdict

@dataclass
class ImageGenerateRequest:
    keyword: str
    category: str

    def __post_init__(self):
        # 만약 빈 문자열("")조차 허용하고 싶지 않다면 아래 로직을 추가하세요.
        if not self.category or self.category.strip() == "":
            raise ValueError("category는 비어 있을 수 없습니다.")

@dataclass
class ImageGenerateResponse:
    imageUrl: str
    refinedPrompt: str

    def to_dict(self):
        return asdict(self)