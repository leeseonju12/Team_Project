-- ════════════════════════════════════════════════════════════
--  DB 마이그레이션
-- ════════════════════════════════════════════════════════════

-- 1. DB 초기화 및 선택
DROP DATABASE IF EXISTS `gather`;
CREATE DATABASE `gather` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `gather`;

-- 2. users 테이블 생성 (기본 구조 예시 - 기존에 있다면 이 부분을 맞춰주세요)
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL,
  `name` VARCHAR(50) NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- ── 1. users 테이블 주소 컬럼 추가 ───────────────────────────
ALTER TABLE users
  ADD COLUMN road_addr_part1 VARCHAR(200) NULL COMMENT '도로명주소 본체 | jusoCallBack: roadAddrPart1',
  ADD COLUMN addr_detail     VARCHAR(100) NULL COMMENT '사용자 입력 상세주소 | jusoCallBack: addrDetail',
  ADD COLUMN business_category    VARCHAR(50)  NULL COMMENT '업종 카테고리',
  ADD COLUMN terms_agreed TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN privacy_agreed TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN location_agreed TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN marketing_consent TINYINT(1) NULL,
  ADD COLUMN event_consent TINYINT(1) NULL;

-- ── 2. business_hours 테이블 ─────────────────────────────────
CREATE TABLE IF NOT EXISTS business_hours (
  id          BIGINT     NOT NULL AUTO_INCREMENT,
  user_id     BIGINT     NOT NULL,
  day_of_week TINYINT    NOT NULL COMMENT '0=월 1=화 2=수 3=목 4=금 5=토 6=일',
  is_open     TINYINT(1) NOT NULL DEFAULT 1,
  open_time   VARCHAR(5) NULL     COMMENT 'HH:mm',
  close_time  VARCHAR(5) NULL     COMMENT 'HH:mm',
  PRIMARY KEY (id),
  UNIQUE KEY uq_bh_user_day (user_id, day_of_week),
  CONSTRAINT fk_bh_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- ── 3. content_settings 테이블 ───────────────────────────────
CREATE TABLE IF NOT EXISTS content_settings (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL,
  intro_template VARCHAR(100) NULL,
  outro_template VARCHAR(100) NULL,
  tone           VARCHAR(20)  NOT NULL DEFAULT '기본',
  emoji_level    VARCHAR(10)  NOT NULL DEFAULT '적당히',
  target_length  INT          NOT NULL DEFAULT 300,
  PRIMARY KEY (id),
  UNIQUE KEY uq_cs_user (user_id),
  CONSTRAINT fk_cs_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- ── 4. (향후) 기존 컬럼 정리 — 코드 정리 완료 후 실행 ─────────
-- ALTER TABLE users DROP COLUMN address;
-- ALTER TABLE users DROP COLUMN address_detail;

-- ── 6. 특정 DB 삭제 및 조회  ──────────────────────────────────
DELETE FROM users WHERE id = 1;
SELECT * FROM users;
SELECT * FROM business_hours;
SELECT * FROM content_settings;