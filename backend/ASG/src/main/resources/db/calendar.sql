-- 1. DB 초기화 및 선택
DROP DATABASE IF EXISTS `gather`;
CREATE DATABASE `gather` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `gather`;

CREATE TABLE `generated_content` (
  `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '테이블 식별자',
  `menu_name` VARCHAR(255) COMMENT '메뉴 이름 (마케팅 카테고리)',
  `content` TEXT COMMENT '생성 컨텐츠 내용 (본문)',
  `hashtags` VARCHAR(255) COMMENT '해시태그',
  `platform` VARCHAR(50) NOT NULL COMMENT '발행 플랫폼 (INSTAGRAM, FACEBOOK, BLOG, KAKAO, COMMUNITY)',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '상태 (PENDING, SCHEDULED, PUBLISHED)',
  `image_url` VARCHAR(255) COMMENT '이미지 url',
  `origin_url` VARCHAR(255) COMMENT '원문 url (참고/소스)',
  `scheduled_date` DATETIME DEFAULT NULL COMMENT '예약 실행 일시',
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성시간',
  `published_at` DATETIME COMMENT '게시 예정/완료 시간',
  `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정시간'
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='AI 생성 컨텐츠 관리 테이블';

-- 일정 미배정(PENDING) 상태의 피트니스 마케팅 데이터 삽입
INSERT INTO `generated_content` 
(menu_name, content, hashtags, platform, STATUS, image_url, origin_url, created_at, published_at)
VALUES 
(
    '오픈 3주년 감사제', 
    '벌써 3주년! 전 종목 30% 할인 들어갑니다. 이번 기회 놓치면 내년에나 올걸요? 지금 바로 상담 예약하세요!', 
    '#3주년이벤트 #헬스타그램 #오운완', 
    'INSTAGRAM', 
    'PENDING', 
    'https://fitness-cdn.com/evt/3rd_anniversary.jpg', 
    NULL, 
    NOW(6), NULL
),
(
    '홈트 루틴 가이드', 
    '바쁜 직장인을 위한 층간소음 없는 10분 전신 유산소 루틴을 공개합니다. 슬로우 버피부터 시작해보세요!', 
    '#홈트레이닝 #직장인운동 #다이어트', 
    'BLOG', 
    'PENDING', 
    'https://fitness-cdn.com/tips/homet-01.jpg', 
    NULL, 
    NOW(6), NULL
),
(
    '오늘의 식단 꿀팁', 
    '운동보다 중요한 식단! 근성장을 돕는 단백질 위주 아침 식단 3가지를 추천해 드립니다.', 
    '#단백질식단 #바디프로필 #식단관리', 
    'KAKAO', 
    'PENDING', 
    NULL, 
    NULL, 
    NOW(6), NULL
),
(
    '바디프로필 챌린지', 
    '성공 확률 100%에 도전하는 바디프로필 챌린지 4기를 모집합니다. 결과로 증명해 드릴게요.', 
    '#바디프로필도전 #인생샷 #헬스이벤트', 
    'FACEBOOK', 
    'PENDING', 
    'https://fitness-cdn.com/challenge/body-profile-4.jpg', 
    NULL, 
    NOW(6), NULL
),
(
    '무료 체형 분석', 
    '우리 동네 주민분들을 위해 1:1 체형 분석을 선착순 무료로 진행합니다. 댓글로 신청하세요!', 
    '#동네운동 #무료체험 #체형교정', 
    'COMMUNITY', 
    'PENDING', 
    NULL, 
    NULL, 
    NOW(6), NULL
);

SELECT * FROM `generated_content`;