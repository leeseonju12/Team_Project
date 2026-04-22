# Spring-Flask AI Image Service

Flask 기반 AI 이미지 생성 API 서버입니다. 사용자가 업종 카테고리와 키워드를 보내면, 키워드를 영어로 번역하고 업종별 프롬프트 템플릿을 결합한 뒤 Stability AI로 이미지를 생성하고 Cloudinary에 업로드합니다.

## 주요 기능

### 1. 이미지 생성 API

- 기본 엔드포인트: `POST /api/ai/generate`
- 입력값:
  - `category`: 업종 카테고리
  - `keyword`: 생성하고 싶은 상품, 메뉴, 공간, 서비스 등의 핵심 키워드
  - `description`: 선택 입력값. 이미지에 반영하고 싶은 세부 설명
- 응답값:
  - `imageUrl`: Cloudinary에 업로드된 생성 이미지 URL
  - `refinedPrompt`: AI 이미지 생성에 사용된 최종 프롬프트

### 2. 업종별 프롬프트 생성 엔진

`prompt_engine.py`는 키워드와 업종을 기반으로 Stability AI에 전달할 영문 프롬프트를 만듭니다.

프롬프트 생성은 다음 4단계로 구성됩니다.

1. `Core Object`: 핵심 피사체를 프롬프트 앞쪽에 배치
2. `Industry Context`: 업종별 배경과 상황 자동 삽입
3. `Style Template`: 업종별 조명, 렌즈, 시각 스타일 결합
4. `Quality Booster`: 상업용 이미지 품질을 높이는 키워드 추가

### 3. 번역, 이미지 생성, 업로드 파이프라인

실제 파이프라인은 `service/image_service.py`의 `ImageService.process_pipeline()`에서 처리합니다.

처리 순서는 다음과 같습니다.

1. MyMemory API로 한국어 키워드와 추가 설명을 영어로 번역
2. `PromptEngine`으로 최종 프롬프트 생성
3. Stability AI API로 이미지 생성
4. Cloudinary에 이미지 업로드
5. 업로드된 이미지 URL과 최종 프롬프트 반환

## 프로젝트 구조

```text
.
├── app.py                         # Flask 앱 생성 및 실행 진입점
├── prompt_engine.py               # 업종별 AI 이미지 프롬프트 생성 엔진
├── config/
│   └── settings.py                # 환경변수와 Cloudinary 설정
├── controller/
│   └── image_controller.py        # 이미지 API 라우터
├── dto/
│   └── image_dto.py               # 요청/응답 DTO
├── exception/
│   └── exceptions.py              # 커스텀 예외
├── repository/
│   ├── cloudinary_repository.py   # Cloudinary 업로드
│   ├── stability_repository.py    # Stability AI 이미지 생성 요청
│   └── translation_repository.py  # MyMemory 번역 요청
├── service/
│   └── image_service.py           # 전체 이미지 생성 파이프라인
└── test/
    ├── test_prompt_engine.py      # 프롬프트 엔진 테스트
    └── test_generator.py          # 통합 파이프라인 테스트 예시
```

## 시작하기

### 1. Python 가상환경 생성

Windows PowerShell 기준:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate
```

### 2. 필요한 패키지 설치

현재 저장소에는 `requirements.txt`가 없으므로 아래 패키지를 직접 설치합니다.

```powershell
pip install flask flask-cors python-dotenv requests cloudinary pytest
```

### 3. 환경변수 설정

프로젝트 루트에 `.env` 파일을 만들고 아래 값을 설정합니다.

```env
STABILITY_API_KEY=your_stability_api_key
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
FLASK_APP=app.py
FLASK_ENV=development
```

API 키 발급 위치:

- Stability AI: <https://platform.stability.ai/>
- Cloudinary: <https://cloudinary.com/console>

주의: `.env`에는 실제 API 키가 들어가므로 GitHub 같은 공개 저장소에 올리지 마세요.

### 4. 서버 실행

```powershell
python app.py
```

정상 실행되면 Flask 서버가 아래 주소에서 실행됩니다.

```text
http://localhost:5000
```

## API 사용 가이드

### 이미지 생성 요청

```text
POST http://localhost:5000/api/ai/generate
Content-Type: application/json
```

요청 예시:

```json
{
  "category": "카페/베이커리",
  "keyword": "아이스 아메리카노",
  "description": "투명한 유리잔에 담긴 차가운 커피와 물방울이 맺힌 표면"
}
```

PowerShell 호출 예시:

```powershell
$body = @{
  category = "카페/베이커리"
  keyword = "아이스 아메리카노"
  description = "투명한 유리잔에 담긴 차가운 커피와 물방울이 맺힌 표면"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:5000/api/ai/generate" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

예상 응답:

```json
{
  "imageUrl": "https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/ai-generated/example.jpg",
  "refinedPrompt": "a close-up of iced americano, featuring ..."
}
```

## 지원 카테고리

`category` 값은 아래 문자열 중 하나를 사용해야 합니다.

```text
카페/베이커리
음식점/식당
미용/뷰티
패션/의류
숙박/펜션
피트니스/스포츠
교육/학원
의료/병원
소매/쇼핑
기타
```

## 실제 이미지 생성 파이프라인 사용 시 확인할 점

실제 파이프라인 실행에 필요한 조건:

- `STABILITY_API_KEY`가 `.env`에 설정되어 있어야 합니다.
- Cloudinary 환경변수 3개가 모두 설정되어 있어야 합니다.
- 외부 API 호출이 가능한 네트워크 환경이어야 합니다.
- Stability AI API 사용량 또는 결제 상태가 정상이어야 합니다.

## 테스트 실행

프롬프트 엔진 단위 테스트:

```powershell
python -m pytest test/test_prompt_engine.py -v
```

통합 파이프라인 테스트 예시:

```powershell
python test/test_generator.py
```

통합 테스트는 MyMemory, Stability AI, Cloudinary 외부 API를 호출할 수 있으므로 API 키와 네트워크 상태가 필요합니다.

## 처음 사용하는 사용자를 위한 순서

1. Python 3을 설치합니다.
2. 프로젝트 폴더에서 가상환경을 생성하고 활성화합니다.
3. 필요한 패키지를 설치합니다.
4. `.env` 파일에 Stability AI와 Cloudinary 키를 입력합니다.
5. `python app.py`로 서버를 실행합니다.
6. `POST /api/ai/generate`로 JSON 요청을 보내 이미지 생성 결과를 확인합니다.

## 문제 해결

### 서버가 실행되지 않을 때

- 가상환경이 활성화되어 있는지 확인합니다.
- Flask 관련 패키지가 설치되어 있는지 확인합니다.
- `python app.py`를 프로젝트 루트에서 실행했는지 확인합니다.

### 이미지 생성이 실패할 때

- `STABILITY_API_KEY`가 비어 있지 않은지 확인합니다.
- Stability AI 계정의 사용 가능 크레딧 또는 결제 상태를 확인합니다.
- Cloudinary 환경변수가 모두 설정되어 있는지 확인합니다.
- 외부 API 호출이 가능한 네트워크 환경인지 확인합니다.

### 카테고리 오류가 발생할 때

요청의 `category` 값이 지원 카테고리 문자열과 정확히 일치해야 합니다. 예를 들어 `카페`, `베이커리`, `cafe`가 아니라 `카페/베이커리`를 사용해야 합니다.
