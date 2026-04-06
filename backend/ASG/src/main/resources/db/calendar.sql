-- 1. DB 초기화 및 선택
DROP DATABASE IF EXISTS `gather`;
CREATE DATABASE `gather` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `gather`;

-- 2. 포스트 테이블 생성
-- Spring Boot 엔티티의 필드명과 일치하도록 구성 (id, title, content, platform, status 등)
CREATE TABLE `posts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '포스트 제목',
    `content` TEXT COMMENT '마케팅 본문 내용',
    `platform` VARCHAR(50) NOT NULL COMMENT '발행 플랫폼 (INSTAGRAM, FACEBOOK, BLOG, KAKAO, COMMUNITY)',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '상태 (PENDING, SCHEDULED, PUBLISHED)',
    `scheduled_date` DATETIME DEFAULT NULL COMMENT '예약 실행 일시',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `border_color` VARCHAR(20) DEFAULT NULL COMMENT 'UI 구분용 컬러코드'
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 피부과 특화 샘플 데이터 적재 (미배정 상태: PENDING)
INSERT INTO `posts` (`title`, `content`, `platform`, `status`, `border_color`) VALUES 
(
    '[이벤트] 릴리이드 물광주사', 
    '속건조 잡는 릴리이드 물광주사 런칭! 💦\n반짝이는 피부 결, 지금 경험해보세요.\n\n✔️ 런칭 특가 선착순 30명\n✔️ 통증 적은 미세 니들 시술\n\n#피부과이벤트 #물광주사 #속건조해결', 
    'INSTAGRAM', 
    'PENDING', 
    '#E1306C'
),
(
    '3040 리프팅 완전정복', 
    '나이가 들수록 처지는 턱선, 해결책은? 🤔\n울세라 vs 써마지 vs 인모드!\n나에게 맞는 리프팅 장비는 무엇인지 카드뉴스로 확인하세요.', 
    'FACEBOOK', 
    'PENDING', 
    '#1877F2'
),
(
    '피부과 전문의가 말하는 여드름 흉터 치료', 
    '안녕하세요, XX피부과입니다. 여드름 흉터는 시기에 맞는 치료가 핵심입니다.\n프락셀 제나와 쥬베룩 볼륨을 활용한 복합 흉터 재생술에 대해 자세히 알아봅니다.\n\n1. 흉터 타입별 진단\n2. 레이저와 스킨부스터의 시너지\n3. 시술 후 재생 관리 꿀팁', 
    'BLOG', 
    'PENDING', 
    '#2DB400'
),
(
    '[공지] 설 연휴 정상진료 안내', 
    '안녕하세요. XX피부과입니다. ✨\n바쁜 일상으로 시술을 미루셨던 분들을 위해 설 연휴 기간 중 04(월)~05(화) 정상 진료를 시행합니다.\n\n- 예약 가능 시간 확인하기\n- 채널 친구 전용 미백 관리 20% 쿠폰 포함', 
    'KAKAO', 
    'PENDING', 
    '#FEE500'
),
(
    '슈링크 유니버스 통증 정도?', 
    '요즘 대세 슈링크 유니버스, 정말 안 아픈가요?\n기존 슈링크보다 시술 시간은 단축되고 통증은 줄어든 이유를 전문의 답변으로 확인하세요.', 
    'COMMUNITY', 
    'PENDING', 
    '#6B7280'
);

-- 4. 확인 쿼리
SELECT * FROM `posts`;