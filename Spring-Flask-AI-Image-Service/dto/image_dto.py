from dataclasses import dataclass, asdict


@dataclass
class ImageGenerateRequest:
    keyword: str
    category: str
    description: str | None = None

    def __post_init__(self):
        # keyword 검증
        if not self.keyword or not self.keyword.strip():
            raise ValueError("keyword는 비어 있을 수 없습니다.")

        # category 검증
        if not self.category or not self.category.strip():
            raise ValueError("category는 비어 있을 수 없습니다.")

        self.keyword = self.keyword.strip()
        self.category = self.category.strip()
        self.description = (self.description or "").strip() or None


@dataclass
class ImageGenerateResponse:
    imageUrl: str
    refinedPrompt: str

    def to_dict(self):
        return asdict(self)


@dataclass
class StabilityImageRequest:
    prompt: str
    negativePrompt: str
    aspectRatio: str = "1:1"
    outputFormat: str = "jpeg"

    def __post_init__(self):
        if not self.prompt or not self.prompt.strip():
            raise ValueError("prompt는 비어 있을 수 없습니다.")

        self.prompt = self.prompt.strip()
        self.negativePrompt = (self.negativePrompt or "").strip()
        self.aspectRatio = self.aspectRatio.strip()
        self.outputFormat = self.outputFormat.strip()

    def to_multipart_files(self):
        files = {
            "prompt": (None, self.prompt),
            "aspect_ratio": (None, self.aspectRatio),
            "output_format": (None, self.outputFormat),
        }

        if self.negativePrompt:
            files["negative_prompt"] = (None, self.negativePrompt)

        return files
