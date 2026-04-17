import os
import logging
from dotenv import load_dotenv
from config.settings import Config
from service.image_service import ImageService

# 로깅 설정 (service에서 사용하는 logger와 동기화)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 1. 환경 변수 로드 및 초기화
load_dotenv()
Config.init_cloudinary()

def test_full_pipeline():
    print("\n" + "="*50)
    print("AI 이미지 생성 파이프라인 통합 테스트 시작")
    print("="*50)

    # 서비스 인스턴스 생성
    # (이미 내부에서 각 Repository를 생성하도록 작성하셨으므로 바로 생성 가능)
    image_service = ImageService()

    # 테스트 데이터 설정 (사용자 입력 시나리오)
    test_cases = [
        {
            "keyword": "최첨단 레이저 장비를 갖춘 깨끗한 치과 진료실",
            "category": "의료/병원"
        }
    ]

    for i, case in enumerate(test_cases, 1):
        print(f"\n[Test Case {i}]")
        print(f"입력 키워드: {case['keyword']}")
        print(f"선택 카테고리: {case['category']}")
        
        try:
            # 2. 파이프라인 실행 (번역 -> 조합 -> 생성 -> 업로드)
            image_url, final_prompt = image_service.process_pipeline(
                keyword=case['keyword'], 
                category=case['category']
            )

            print(f"✅ 번역 및 조합된 최종 프롬프트:\n   > {final_prompt}")
            print(f"✅ 생성된 이미지 URL:\n   > {image_url}")

        except Exception as e:
            # 정의하신 커스텀 에러(TranslationError, GenerationError 등)가 여기서 잡힙니다.
            print(f"❌ 파이프라인 오류 발생: {type(e).__name__}")
            print(f"   메시지: {str(e)}")

    print("\n" + "="*50)
    print("테스트 종료")
    print("="*50)

if __name__ == "__main__":
    load_dotenv()
    print(f"DEBUG: Config Key -> {Config.STABILITY_API_KEY}")
    test_full_pipeline()
