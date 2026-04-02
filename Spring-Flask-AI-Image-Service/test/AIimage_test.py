import requests
from pathlib import Path

# ──────────────────────────────────────────────────────
# API 키 설정
# ──────────────────────────────────────────────────────
STABILITY_KEY = ""  # ← 여기에 키 입력


# ──────────────────────────────────────────────────────
# 공식 예제의 send_generation_request 함수 구현
# ──────────────────────────────────────────────────────
def send_generation_request(host, params):
    headers = {
        "Authorization": f"Bearer {STABILITY_KEY}",
        "Accept": "image/*"
    }

    # multipart/form-data 형식으로 전송 (Stability AI 필수 요구사항)
    files = {}
    image_params = {}
    for key, value in params.items():
        if value is not None and value != "":
            image_params[key] = (None, str(value))
        files = image_params if image_params else {"none": ""}

    response = requests.post(host, headers=headers, files=files)
    if not response.ok:
        raise Exception(f"HTTP {response.status_code}: {response.text}")
    return response


# ──────────────────────────────────────────────────────
# 파라미터 설정
# ──────────────────────────────────────────────────────
prompt = (
    "architectural interior photography, "
    "modern yoga studio, empty room, "
    "row of rolled green yoga mats on light oak wood floor, "
    "floor-to-ceiling mirrors on white walls, "
    "soft cool morning sunlight from large windows, "
    "minimalist clean space, high contrast, sharp focus, "
    "professional commercial photography"
)

# 목적에 맞게 추린 negative_prompt
# (텍스트/워터마크 방지 + 품질 저하 방지 + 사람 제거만 핵심으로 유지)
negative_prompt = (
    "people, person, human, "
    "text, watermark, logo, signature, "
    "blurry, low resolution, distorted, ugly"
)

aspect_ratio   = "1:1"         # 정사각형 (core 모델 최저 비용)
seed           = 0             # 0 = 랜덤 시드
style_preset   = "photographic"  # 사진 스타일 강제 적용
output_format  = "jpeg"        # jpeg = 용량 작음 (png 대비 저렴하게 저장)

# ──────────────────────────────────────────────────────
# 모델 선택: core → 3크레딧/장 (무료 25크레딧으로 8장 가능, 가장 저렴)
# ──────────────────────────────────────────────────────
host = "https://api.stability.ai/v2beta/stable-image/generate/core"

params = {
    "prompt":          prompt,
    "negative_prompt": negative_prompt,
    "aspect_ratio":    aspect_ratio,
    "seed":            seed,
    "output_format":   output_format,
    "style_preset":    style_preset,
}

# ──────────────────────────────────────────────────────
# 이미지 생성 요청
# ──────────────────────────────────────────────────────
print(f"모델   : Stable Image Core  ($0.030 / 3크레딧)")
print(f"사이즈 : {aspect_ratio}  |  포맷: {output_format}")
print(f"스타일 : {style_preset}")
print(f"프롬프트:\n  {prompt}")
print(f"네거티브:\n  {negative_prompt}\n")

response = send_generation_request(host, params)

# ──────────────────────────────────────────────────────
# 응답 처리 (공식 예제 구조 동일)
# ──────────────────────────────────────────────────────
output_image  = response.content
finish_reason = response.headers.get("finish-reason")
seed          = response.headers.get("seed")

# NSFW 필터 체크
if finish_reason == "CONTENT_FILTERED":
    raise Warning("Generation failed NSFW classifier")

# 저장
Path("outputs").mkdir(exist_ok=True)
generated = f"outputs/yoga_studio_{seed}.{output_format}"
with open(generated, "wb") as f:
    f.write(output_image)

print(f"완료! → {generated}  (seed: {seed})")