-- 1. gather DB 사용
USE gather;

-- 2. 기존 테이블이 있다면 삭제 (초기화)
DROP TABLE IF EXISTS customer_feedback;
DROP TABLE IF EXISTS feedback_source;

-- 3. customer_feedback 테이블 생성
CREATE TABLE customer_feedback (
    feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    TYPE VARCHAR(20) NOT NULL COMMENT 'REVIEW, COMMENT (백엔드에서 자동 입력)',
    STATUS VARCHAR(30) DEFAULT 'UNRESOLVED',
    ai_reply TEXT,
    ai_status VARCHAR(20) DEFAULT 'IDLE',
    sent_reply TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES feedback_source(source_id) ON DELETE CASCADE
);

CREATE TABLE feedback_source (
    source_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(100) NOT NULL,
    platform VARCHAR(30) NOT NULL COMMENT 'NAVER, KAKAO, GOOGLE, INSTAGRAM, FACEBOOK',
    original_text TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

##=============

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE customer_feedback;
TRUNCATE TABLE feedback_source;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO feedback_source (author_name, platform, original_text) VALUES
('김철수', 'NAVER', '퇴근길에 들러서 포장했는데 식어도 바삭하고 맛있네요. 양념이 딱 제 취향입니다.'),
('이영희', 'KAKAO', '주차장이 협소해서 조금 불편했지만, 매니저님이 친절하게 안내해주셔서 감사했습니다.'),
('박준호', 'GOOGLE', '친구 추천으로 방문했는데 커피 산미 밸런스가 아주 훌륭합니다. 재방문 의사 100%'),
('정수민', 'INSTAGRAM', '헐 이거 신상이에요?? 당장 내일 먹으러 갑니다 ㅠㅠ @지영 같이가자'),
('최동석', 'FACEBOOK', '주말 단체 예약 20명 가능할까요? 동호회 모임 장소로 보고 있습니다.'),
('윤이나', 'NAVER', '아이들 데리고 갔는데 아기 의자도 넉넉하고 덜 맵게 조리해주셔서 너무 좋았어요.'),
('강민준', 'KAKAO', '치킨무를 빼달라고 요청했는데 그대로 왔네요. 환경 생각해서 뺀 건데 아쉽습니다.'),
('한지혜', 'GOOGLE', '외국인 동료들과 회식으로 갔는데 다들 한국 치킨 최고라고 난리네요 ㅋㅋㅋ'),
('송지훈', 'INSTAGRAM', '인테리어 완전 취향저격✨ 사진 백장 찍고 갑니다 📸'),
('오민서', 'FACEBOOK', '혹시 생일자 방문하면 뭐 할인이나 서비스 같은 거 있나요?'),
('유재석', 'NAVER', '기본 안주로 나오는 나초가 너무 맛있어요. 맥주가 술술 들어갑니다.'),
('김태희', 'KAKAO', '화장실에 가그린이랑 면봉 있는 거 보고 감동받았습니다. 센스 대박!'),
('이광수', 'GOOGLE', '배달시키면 한 시간 걸린다더니 30분 만에 왔어요. 빠른 배달 감사합니다.'),
('박명수', 'INSTAGRAM', '아 다이어트 중인데 이 야밤에 이걸 봐버렸네... 책임지세요 사장님'),
('하하', 'FACEBOOK', '여기 쿠폰 도장 10개 모으면 치킨 한 마리 무료 맞죠? 쿠폰 다 채워갑니다 ㅎㅎ'),
('노홍철', 'NAVER', '콜라 사이즈 업그레이드 이벤트 너무 좋습니다. 치킨엔 역시 제로콜라!'),
('정형돈', 'KAKAO', '창가 자리 뷰가 정말 좋네요. 비 오는 날 커피 마시면서 힐링하고 갑니다.'),
('길성준', 'GOOGLE', '알바생분이 메뉴 추천을 너무 찰떡같이 해주셨어요. 덕분에 잘 먹었습니다.'),
('전진', 'INSTAGRAM', '팝업스토어는 며칠까지 운영하나요? 주말에만 열리나요?'),
('김동완', 'FACEBOOK', '배민으로 시켰는데 리뷰 이벤트 참여 깜빡했어요ㅠㅠ 다음번엔 꼭 할게요!'),
('신혜성', 'NAVER', '순살은 백퍼센트 닭다리살인가요? 퍽퍽한 살이 하나도 없어서 너무 부드러워요.'),
('이민우', 'KAKAO', '저번보다 양이 좀 줄어든 것 같은 건 기분 탓인가요? 맛은 여전히 좋습니다.'),
('에릭', 'GOOGLE', '비건 메뉴 옵션이 있어서 너무 좋았습니다. 선택지가 다양하네요.'),
('앤디', 'INSTAGRAM', '텀블러 굿즈 품절인가요? 언제 재입고 되나요 ㅠㅠ'),
('박지성', 'FACEBOOK', '축구 보는 날엔 무조건 소셜다모아 치킨입니다. 스크린도 커서 경기 보기 딱 좋아요.'),
('손흥민', 'NAVER', '튀김옷이 얇아서 덜 느끼하고 질리지 않는 맛입니다. 한화 이글스 화이팅!'),
('이강인', 'KAKAO', '주문 키오스크가 한 대밖에 없어서 사람 많을 땐 눈치 보여요. 한 대 더 놔주세요.'),
('김민재', 'GOOGLE', '매장이 너무 깨끗해서 바닥에서 광이 납니다. 위생 관리가 철저해 보여요.'),
('황희찬', 'INSTAGRAM', '오픈런 성공!! 일찍 온 보람이 있네요 ㅋㅋㅋ'),
('조규성', 'FACEBOOK', '메뉴판에 칼로리 표시해 주시면 안 될까요? 식단 중이라 궁금합니다.');

-- 2. 원본 데이터를 바탕으로 customer_feedback (관리 데이터) 자동 생성 및 연결
-- PLATFORM이 INSTAGRAM, FACEBOOK이면 'COMMENT', 나머지는 'REVIEW'로 자동 분류
INSERT INTO customer_feedback (source_id, TYPE, STATUS, ai_status)
SELECT 
    source_id,
    CASE 
        WHEN platform IN ('INSTAGRAM', 'FACEBOOK') THEN 'COMMENT' 
        ELSE 'REVIEW' 
    END AS TYPE,
    'UNRESOLVED' AS STATUS,
    'IDLE' AS ai_status
FROM feedback_source;