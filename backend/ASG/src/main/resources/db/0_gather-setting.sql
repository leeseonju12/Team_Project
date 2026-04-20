/* 
260414 마지막 수정

정렬 및 실행 순서
create > index > alter > insert

[실행 순서 보장 근거]
  platform       : brand_platform 의 platform_id FK 선행 필요
  brand          : brand_platform 의 brand_id FK 선행 필요
  brand_platform : content_post / platform_metric_daily / performance_impact_analysis 의 brand_platform_id FK 선행 필요
  ※ 채널성과 더미가 brand_platform_id 1,2 를 참조하므로 더미보다 반드시 선행
  feedback_source: customer_feedback 의 source_id FK 선행 필요
  date_dimension : content_post(published_date_key) / *_metric_daily 의 date_key FK 선행 필요
  content_post   : post_metric_daily 의 post_id FK 선행 필요
  users          : brand UPDATE(user_id) 는 users INSERT 이후 → 마지막 블록

[customerCenter 관련 테이블]
  inquiry        : type / body / attachment_names / attachment_saved_names 컬럼 포함, status 기본값 '미처리'
  notice         : 공지사항 (독립 테이블, FK 없음)
  faq            : 자주묻는질문 (독립 테이블, FK 없음)
  reply          : inquiry_id 논리적 참조 (물리 FK 없음 — cascade 충돌 방지)
  admin_user     : 고객센터 관리자 계정 (독립 테이블, FK 없음)
*/

DROP DATABASE IF EXISTS gather;
CREATE DATABASE gather;
USE gather;


-- ══════════════════════════════════════════════
-- CREATE TABLE
-- ══════════════════════════════════════════════

-- users
CREATE TABLE `users` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
  `email`              VARCHAR(100) NOT NULL,
  `name`               VARCHAR(50)  NULL,
  `nickname`           VARCHAR(50)  NULL,
  `provider`           VARCHAR(20)  NOT NULL COMMENT 'kakao / naver / google',
  `provider_id`        VARCHAR(255) NOT NULL,
  `contact_phone`      VARCHAR(20)  NULL,
  `company_name`       VARCHAR(100) NULL,
  `business_category`  VARCHAR(50)  NULL,
  `preferred_channel`  VARCHAR(20)  NULL,
  `store_phone_number` VARCHAR(20)  NULL,
  `road_addr_part1`    VARCHAR(200) NULL COMMENT '도로명주소 본체',
  `addr_detail`        VARCHAR(100) NULL COMMENT '상세주소',
  `terms_agreed`       TINYINT(1)   NOT NULL DEFAULT 0,
  `privacy_agreed`     TINYINT(1)   NOT NULL DEFAULT 0,
  `location_agreed`    TINYINT(1)   NOT NULL DEFAULT 0,
  `marketing_consent`  TINYINT(1)   NULL,
  `event_consent`      TINYINT(1)   NULL,
  `signup_completed`   TINYINT(1)   NOT NULL DEFAULT 0,
  `status`             VARCHAR(30)  NOT NULL DEFAULT 'SIGNUP_PENDING' COMMENT 'SIGNUP_PENDING / ACTIVE',
  `role`               VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
  `created_at`         DATETIME     NULL,
  `updated_at`         DATETIME     NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_provider` (`provider`, `provider_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- business_hours
CREATE TABLE `business_hours` (
  `id`          BIGINT     NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT     NOT NULL,
  `day_of_week` TINYINT    NOT NULL COMMENT '0=월 1=화 2=수 3=목 4=금 5=토 6=일',
  `is_open`     TINYINT(1) NOT NULL DEFAULT 1,
  `open_time`   VARCHAR(5) NULL     COMMENT 'HH:mm',
  `close_time`  VARCHAR(5) NULL     COMMENT 'HH:mm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bh_user_day` (`user_id`, `day_of_week`),
  CONSTRAINT `fk_bh_user` FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- content_settings
CREATE TABLE `content_settings` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT       NOT NULL,
  `intro_template` VARCHAR(100) NULL,
  `outro_template` VARCHAR(100) NULL,
  `tone`           VARCHAR(20)  NOT NULL DEFAULT '기본',
  `emoji_level`    VARCHAR(10)  NOT NULL DEFAULT '적당히',
  `target_length`  INT          NOT NULL DEFAULT 300,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cs_user` (`user_id`),
  CONSTRAINT `fk_cs_user` FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- brand
CREATE TABLE `brand` (
  `brand_id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT       NULL         COMMENT '유저 ID (로그인 연동 후 사용)',
  `brand_name`        VARCHAR(100) NOT NULL     COMMENT '브랜드명',
  `service_name`      TEXT         NULL         COMMENT 'sns 계정 아이디',
  `industry_type`     VARCHAR(50)  NULL         COMMENT '업종',
  `location_name`     VARCHAR(150) NULL         COMMENT '매장명/지점명',
  `address`           VARCHAR(255) NULL         COMMENT '도로명 주소',
  `phone`             VARCHAR(20)  NULL         COMMENT '가게 전화번호',
  `profile_image_url` VARCHAR(500) NULL         COMMENT '대표 이미지 URL',
  `created_at`        DATETIME     NOT NULL     COMMENT '생성일시',
  `updated_at`        DATETIME     NOT NULL     COMMENT '수정일시',
  PRIMARY KEY (`brand_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='브랜드/매장 기준 정보';

-- brand_operation_profile
CREATE TABLE `brand_operation_profile` (
  `operation_profile_id`   BIGINT      NOT NULL AUTO_INCREMENT,
  `brand_id`               BIGINT      UNIQUE NOT NULL COMMENT '브랜드 ID',
  `open_time`              TIME        NULL   COMMENT '영업 시작 시간',
  `close_time`             TIME        NULL   COMMENT '영업 종료 시간',
  `regular_closed_weekday` TINYINT     NULL   COMMENT '정기 휴무 요일(1=월 ~ 7=일)',
  `weekend_impact_type`    VARCHAR(20) NULL   COMMENT 'positive / neutral / negative',
  `holiday_impact_type`    VARCHAR(20) NULL   COMMENT 'positive / neutral / negative',
  `peak_business_time`     VARCHAR(50) NULL   COMMENT '피크 영업 시간대',
  `note`                   TEXT        NULL   COMMENT '운영 특이사항',
  `created_at`             DATETIME    NOT NULL COMMENT '생성일시',
  `updated_at`             DATETIME    NOT NULL COMMENT '수정일시',
  PRIMARY KEY (`operation_profile_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='사업장 운영 특성 정보';

-- platform
CREATE TABLE `platform` (
  `platform_id`   BIGINT      NOT NULL AUTO_INCREMENT,
  `platform_code` VARCHAR(30) UNIQUE NOT NULL COMMENT 'instagram / facebook / naver / kakao',
  `platform_name` VARCHAR(50) NOT NULL,
  `brand_color`   VARCHAR(20) NULL,
  `is_active`     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`platform_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='플랫폼 마스터';

-- brand_platform
CREATE TABLE `brand_platform` (
  `brand_platform_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `brand_id`          BIGINT       NOT NULL,
  `platform_id`       BIGINT       NOT NULL,
  `channel_name`      VARCHAR(100) NULL,
  `channel_url`       VARCHAR(255) NULL,
  `is_connected`      BOOLEAN      NOT NULL DEFAULT FALSE,
  `access_token`      TEXT         NULL     COMMENT 'SNS 액세스 토큰',
  `refresh_token`     TEXT         NULL     COMMENT 'SNS 리프레시 토큰',
  `token_expires_at`  DATETIME     NULL     COMMENT '토큰 만료 일시',
  `token_status`      VARCHAR(20)  NULL     DEFAULT 'ACTIVE' COMMENT 'ACTIVE / EXPIRED',
  `connected_at`      DATETIME     NULL,
  `created_at`        DATETIME     NOT NULL,
  `updated_at`        DATETIME     NOT NULL,
  PRIMARY KEY (`brand_platform_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='브랜드별 운영 채널 정보';

-- feedback_source
CREATE TABLE `feedback_source` (
  `source_id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `author_name`   VARCHAR(255) NULL,
  `created_at`    DATETIME(6)  NULL,
  `original_text` TEXT         NULL,
  `platform`      ENUM('FACEBOOK','GOOGLE','INSTAGRAM','KAKAO','NAVER') NULL,
  PRIMARY KEY (`source_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- customer_feedback
CREATE TABLE `customer_feedback` (
  `feedback_id` BIGINT NOT NULL AUTO_INCREMENT,
  `ai_reply`    TEXT   NULL,
  `ai_status`   ENUM('DONE','IDLE') NULL,
  `created_at`  DATETIME(6) NULL,
  `sent_reply`  TEXT   NULL,
  `status`      ENUM('CHECKED','COMPLETED','SENDING','UNCHECKED','UNRESOLVED') NULL,
  `type`        ENUM('COMMENT','REVIEW') NULL,
  `updated_at`  DATETIME(6) NULL,
  `source_id`   BIGINT NULL,
  PRIMARY KEY (`feedback_id`),
  UNIQUE KEY `uk_customer_feedback_source` (`source_id`),
  CONSTRAINT `fk_customer_feedback_source`
    FOREIGN KEY (`source_id`) REFERENCES `feedback_source` (`source_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- 채널 성과 분석
CREATE TABLE `date_dimension` (
  `date_key`        INT         NOT NULL,
  `full_date`       DATE        UNIQUE NOT NULL,
  `year_no`         INT         NOT NULL,
  `half_no`         TINYINT     NOT NULL,
  `quarter_no`      TINYINT     NOT NULL,
  `month_no`        TINYINT     NOT NULL,
  `month_name`      VARCHAR(20) NULL,
  `week_of_year`    TINYINT     NULL,
  `week_of_month`   TINYINT     NULL,
  `day_of_month`    TINYINT     NOT NULL,
  `day_of_week`     TINYINT     NOT NULL,
  `day_name_kr`     VARCHAR(10) NOT NULL,
  `is_weekend`      BOOLEAN     NOT NULL DEFAULT FALSE,
  `is_holiday`      BOOLEAN     NOT NULL DEFAULT FALSE,
  `is_business_day` BOOLEAN     NOT NULL DEFAULT TRUE,
  `is_month_start`  BOOLEAN     NOT NULL DEFAULT FALSE,
  `is_month_end`    BOOLEAN     NOT NULL DEFAULT FALSE,
  `is_year_start`   BOOLEAN     NOT NULL DEFAULT FALSE,
  `is_year_end`     BOOLEAN     NOT NULL DEFAULT FALSE,
  `season_code`     VARCHAR(20) NULL,
  `holiday_name`    VARCHAR(100) NULL,
  PRIMARY KEY (`date_key`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `content_post` (
  `post_id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `brand_platform_id`  BIGINT       NOT NULL,
  `post_title`         VARCHAR(200) NULL,
  `post_type`          VARCHAR(50)  NULL,
  `post_body`          TEXT         NULL,
  `published_at`       DATETIME     NULL,
  `published_date_key` INT          NULL,
  `published_hour`     TINYINT      NULL,
  `status`             VARCHAR(30)  NOT NULL DEFAULT 'published',
  `created_at`         DATETIME     NOT NULL,
  `updated_at`         DATETIME     NOT NULL,
  PRIMARY KEY (`post_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `post_metric_daily` (
  `post_metric_daily_id` BIGINT   NOT NULL AUTO_INCREMENT,
  `post_id`              BIGINT   NOT NULL,
  `date_key`             INT      NOT NULL,
  `view_count`           INT      NOT NULL DEFAULT 0,
  `like_count`           INT      NOT NULL DEFAULT 0,
  `comment_count`        INT      NOT NULL DEFAULT 0,
  `share_count`          INT      NOT NULL DEFAULT 0,
  `follower_gain`        INT      NOT NULL DEFAULT 0,
  `review_count`         INT      NOT NULL DEFAULT 0,
  `save_count`           INT      NOT NULL DEFAULT 0,
  `click_count`          INT      NOT NULL DEFAULT 0,
  `created_at`           DATETIME NOT NULL,
  PRIMARY KEY (`post_metric_daily_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_metric_daily` (
  `platform_metric_daily_id` BIGINT        NOT NULL AUTO_INCREMENT,
  `brand_platform_id`        BIGINT        NOT NULL,
  `date_key`                 INT           NOT NULL,
  `total_views`              INT           NOT NULL DEFAULT 0,
  `total_likes`              INT           NOT NULL DEFAULT 0,
  `total_comments`           INT           NOT NULL DEFAULT 0,
  `total_shares`             INT           NOT NULL DEFAULT 0,
  `total_reviews`            INT           NOT NULL DEFAULT 0,
  `follower_growth`          INT           NOT NULL DEFAULT 0,
  `engagement_score`         DECIMAL(10,2) NOT NULL DEFAULT 0,
  `created_at`               DATETIME      NOT NULL,
  PRIMARY KEY (`platform_metric_daily_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_hourly_metric` (
  `hourly_metric_id`  BIGINT   NOT NULL AUTO_INCREMENT,
  `brand_platform_id` BIGINT   NOT NULL,
  `date_key`          INT      NOT NULL,
  `hour_of_day`       TINYINT  NOT NULL,
  `view_count`        INT      NOT NULL DEFAULT 0,
  `like_count`        INT      NOT NULL DEFAULT 0,
  `comment_count`     INT      NOT NULL DEFAULT 0,
  `share_count`       INT      NOT NULL DEFAULT 0,
  `created_at`        DATETIME NOT NULL,
  PRIMARY KEY (`hourly_metric_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `search_performance_daily` (
  `search_perf_id` BIGINT      NOT NULL AUTO_INCREMENT,
  `brand_id`       BIGINT      NOT NULL,
  `date_key`       INT         NOT NULL,
  `search_engine`  VARCHAR(30) NOT NULL,
  `search_count`   INT         NOT NULL DEFAULT 0,
  `click_count`    INT         NOT NULL DEFAULT 0,
  `created_at`     DATETIME    NOT NULL,
  PRIMARY KEY (`search_perf_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `keyword_master` (
  `keyword_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `brand_id`     BIGINT       NOT NULL,
  `keyword_text` VARCHAR(100) NOT NULL,
  `keyword_type` VARCHAR(30)  NULL,
  `created_at`   DATETIME     NOT NULL,
  PRIMARY KEY (`keyword_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `keyword_performance_daily` (
  `keyword_perf_id` BIGINT   NOT NULL AUTO_INCREMENT,
  `keyword_id`      BIGINT   NOT NULL,
  `date_key`        INT      NOT NULL,
  `search_count`    INT      NOT NULL DEFAULT 0,
  `click_count`     INT      NOT NULL DEFAULT 0,
  `rank_no`         INT      NULL,
  `created_at`      DATETIME NOT NULL,
  PRIMARY KEY (`keyword_perf_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_metric_monthly_summary` (
  `monthly_summary_id`  BIGINT        NOT NULL AUTO_INCREMENT,
  `brand_platform_id`   BIGINT        NOT NULL,
  `year_no`             INT           NOT NULL,
  `month_no`            TINYINT       NOT NULL,
  `start_date_key`      INT           NOT NULL,
  `end_date_key`        INT           NOT NULL,
  `total_views`         INT           NOT NULL DEFAULT 0,
  `total_likes`         INT           NOT NULL DEFAULT 0,
  `total_comments`      INT           NOT NULL DEFAULT 0,
  `total_shares`        INT           NOT NULL DEFAULT 0,
  `total_reviews`       INT           NOT NULL DEFAULT 0,
  `follower_growth`     INT           NOT NULL DEFAULT 0,
  `engagement_score`    DECIMAL(10,2) NOT NULL DEFAULT 0,
  `avg_daily_views`     DECIMAL(12,2) NOT NULL DEFAULT 0,
  `weekend_views`       INT           NOT NULL DEFAULT 0,
  `weekday_views`       INT           NOT NULL DEFAULT 0,
  `holiday_views`       INT           NOT NULL DEFAULT 0,
  `weekend_ratio`       DECIMAL(5,2)  NOT NULL DEFAULT 0,
  `channel_share_ratio` DECIMAL(5,2)  NOT NULL DEFAULT 0,
  `created_at`          DATETIME      NOT NULL,
  `updated_at`          DATETIME      NOT NULL,
  PRIMARY KEY (`monthly_summary_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_metric_yearly_summary` (
  `yearly_summary_id`   BIGINT        NOT NULL AUTO_INCREMENT,
  `brand_platform_id`   BIGINT        NOT NULL,
  `year_no`             INT           NOT NULL,
  `start_date_key`      INT           NOT NULL,
  `end_date_key`        INT           NOT NULL,
  `total_views`         INT           NOT NULL DEFAULT 0,
  `total_likes`         INT           NOT NULL DEFAULT 0,
  `total_comments`      INT           NOT NULL DEFAULT 0,
  `total_shares`        INT           NOT NULL DEFAULT 0,
  `total_reviews`       INT           NOT NULL DEFAULT 0,
  `follower_growth`     INT           NOT NULL DEFAULT 0,
  `engagement_score`    DECIMAL(10,2) NOT NULL DEFAULT 0,
  `avg_monthly_views`   DECIMAL(12,2) NOT NULL DEFAULT 0,
  `weekend_views`       INT           NOT NULL DEFAULT 0,
  `weekday_views`       INT           NOT NULL DEFAULT 0,
  `holiday_views`       INT           NOT NULL DEFAULT 0,
  `weekend_ratio`       DECIMAL(5,2)  NOT NULL DEFAULT 0,
  `channel_share_ratio` DECIMAL(5,2)  NOT NULL DEFAULT 0,
  `created_at`          DATETIME      NOT NULL,
  `updated_at`          DATETIME      NOT NULL,
  PRIMARY KEY (`yearly_summary_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_metric_monthly_trend` (
  `monthly_trend_id`  BIGINT   NOT NULL AUTO_INCREMENT,
  `brand_platform_id` BIGINT   NOT NULL,
  `year_no`           INT      NOT NULL,
  `month_no`          TINYINT  NOT NULL,
  `views`             INT      NOT NULL DEFAULT 0,
  `likes`             INT      NOT NULL DEFAULT 0,
  `comments`          INT      NOT NULL DEFAULT 0,
  `shares`            INT      NOT NULL DEFAULT 0,
  `followers`         INT      NOT NULL DEFAULT 0,
  `created_at`        DATETIME NOT NULL,
  PRIMARY KEY (`monthly_trend_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `platform_metric_yearly_trend` (
  `yearly_trend_id`   BIGINT   NOT NULL AUTO_INCREMENT,
  `brand_platform_id` BIGINT   NOT NULL,
  `year_no`           INT      NOT NULL,
  `views`             INT      NOT NULL DEFAULT 0,
  `likes`             INT      NOT NULL DEFAULT 0,
  `comments`          INT      NOT NULL DEFAULT 0,
  `shares`            INT      NOT NULL DEFAULT 0,
  `followers`         INT      NOT NULL DEFAULT 0,
  `created_at`        DATETIME NOT NULL,
  PRIMARY KEY (`yearly_trend_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `performance_impact_analysis` (
  `impact_analysis_id`   BIGINT        NOT NULL AUTO_INCREMENT,
  `brand_platform_id`    BIGINT        NOT NULL,
  `analysis_period_type` VARCHAR(20)   NOT NULL,
  `base_year`            INT           NOT NULL,
  `base_month`           TINYINT       NULL,
  `weekend_effect_score` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `holiday_effect_score` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `best_day_of_week`     TINYINT       NULL,
  `worst_day_of_week`    TINYINT       NULL,
  `best_hour_range`      VARCHAR(50)   NULL,
  `created_at`           DATETIME      NOT NULL,
  PRIMARY KEY (`impact_analysis_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `strategy_recommendation` (
  `strategy_id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `brand_id`            BIGINT      NOT NULL,
  `period_type`         VARCHAR(20) NOT NULL,
  `based_on_start_date` DATE        NOT NULL,
  `based_on_end_date`   DATE        NOT NULL,
  `summary_text`        TEXT        NULL,
  `generated_at`        DATETIME    NOT NULL,
  `created_at`          DATETIME    NOT NULL,
  PRIMARY KEY (`strategy_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `strategy_recommendation_item` (
  `strategy_item_id`      BIGINT       NOT NULL AUTO_INCREMENT,
  `strategy_id`           BIGINT       NOT NULL,
  `sort_order`            INT          NOT NULL,
  `recommendation_title`  VARCHAR(100) NOT NULL,
  `platform_id`           BIGINT       NOT NULL,
  `recommended_time_slot` VARCHAR(50)  NULL,
  `content_type`          VARCHAR(100) NULL,
  `detail_text`           TEXT         NULL,
  `created_at`            DATETIME     NOT NULL,
  PRIMARY KEY (`strategy_item_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `inquiry` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
  `type`                  VARCHAR(50)  NULL     COMMENT '문의 유형 (가입·연동/콘텐츠 생성/시스템오류/계정/기타)',
  `email`                 VARCHAR(255) NOT NULL,
  `title`                 VARCHAR(255) NOT NULL,
  `body`                  TEXT         NULL     COMMENT '문의 본문 (content 컬럼과 동일 역할, customerCenter 호환용)',
  `content`               TEXT         NULL     COMMENT '문의 본문 (myPage 기존 호환용)',
  `status`                VARCHAR(50)  NOT NULL DEFAULT '미처리' COMMENT '미처리 / 처리중 / 처리완료',
  `attachment_names`      TEXT         NULL     COMMENT '첨부파일 원본 파일명 (콤마 구분)',
  `attachment_saved_names` TEXT        NULL     COMMENT '첨부파일 서버 저장명 (콤마 구분)',
  `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- notice (공지사항)
CREATE TABLE `notice` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(200) NOT NULL,
  `content`    LONGTEXT     NOT NULL,
  `category`   VARCHAR(30)  NOT NULL DEFAULT '공지' COMMENT '공지 / 업데이트 / 점검',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='공지사항';

-- faq (자주 묻는 질문)
CREATE TABLE `faq` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `category`   VARCHAR(100) NOT NULL COMMENT '가입·연동 / 콘텐츠 생성 / 시스템오류 / 계정',
  `question`   VARCHAR(255) NOT NULL,
  `answer`     LONGTEXT     NOT NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='자주 묻는 질문';

-- reply (문의 답변 — inquiry_id 논리적 참조, 물리 FK 없음)
CREATE TABLE `reply` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `inquiry_id` BIGINT   NOT NULL COMMENT 'inquiry.id 논리적 참조',
  `content`    TEXT     NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reply_inquiry_id` (`inquiry_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='문의 답변';

-- admin_user (고객센터 관리자 계정)
CREATE TABLE `admin_user` (
  `id`       BIGINT       NOT NULL AUTO_INCREMENT,
  `login_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '관리자 로그인 ID',
  `password` VARCHAR(255) NOT NULL,
  `name`     VARCHAR(50)  NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='고객센터 관리자 계정';


-- ══════════════════════════════════════════════
-- CREATE INDEX
-- ══════════════════════════════════════════════
CREATE UNIQUE INDEX `brand_platform_index_0`                    ON `brand_platform`                  (`brand_id`, `platform_id`);
CREATE UNIQUE INDEX `post_metric_daily_index_6`                 ON `post_metric_daily`               (`post_id`, `date_key`);
CREATE UNIQUE INDEX `platform_metric_daily_index_8`             ON `platform_metric_daily`           (`brand_platform_id`, `date_key`);
CREATE UNIQUE INDEX `platform_hourly_metric_index_10`           ON `platform_hourly_metric`          (`brand_platform_id`, `date_key`, `hour_of_day`);
CREATE UNIQUE INDEX `search_performance_daily_index_12`         ON `search_performance_daily`        (`brand_id`, `search_engine`, `date_key`);
CREATE UNIQUE INDEX `keyword_master_index_14`                   ON `keyword_master`                  (`brand_id`, `keyword_text`);
CREATE UNIQUE INDEX `keyword_performance_daily_index_15`        ON `keyword_performance_daily`       (`keyword_id`, `date_key`);
CREATE UNIQUE INDEX `platform_metric_monthly_summary_index_18`  ON `platform_metric_monthly_summary` (`brand_platform_id`, `year_no`, `month_no`);
CREATE UNIQUE INDEX `platform_metric_yearly_summary_index_21`   ON `platform_metric_yearly_summary`  (`brand_platform_id`, `year_no`);
CREATE UNIQUE INDEX `platform_metric_monthly_trend_index_23`    ON `platform_metric_monthly_trend`   (`brand_platform_id`, `year_no`, `month_no`);
CREATE UNIQUE INDEX `platform_metric_yearly_trend_index_24`     ON `platform_metric_yearly_trend`    (`brand_platform_id`, `year_no`);
CREATE UNIQUE INDEX `performance_impact_analysis_index_25`      ON `performance_impact_analysis`     (`brand_platform_id`, `analysis_period_type`, `base_year`, `base_month`);
CREATE UNIQUE INDEX `strategy_recommendation_item_index_27`     ON `strategy_recommendation_item`    (`strategy_id`, `sort_order`);


-- ══════════════════════════════════════════════
-- ALTER TABLE
-- ══════════════════════════════════════════════
ALTER TABLE `content_settings`
  ADD COLUMN `preferred_sns` VARCHAR(100) NULL COMMENT 'comma-separated: instagram,naver,kakao,facebook';

ALTER TABLE `brand`                           ADD FOREIGN KEY (`user_id`)            REFERENCES `users`           (`id`);
ALTER TABLE `brand_operation_profile`         ADD FOREIGN KEY (`brand_id`)           REFERENCES `brand`           (`brand_id`);
ALTER TABLE `brand_platform`                  ADD FOREIGN KEY (`brand_id`)           REFERENCES `brand`           (`brand_id`);
ALTER TABLE `brand_platform`                  ADD FOREIGN KEY (`platform_id`)        REFERENCES `platform`        (`platform_id`);
ALTER TABLE `content_post`                    ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `content_post`                    ADD FOREIGN KEY (`published_date_key`) REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `post_metric_daily`               ADD FOREIGN KEY (`post_id`)            REFERENCES `content_post`    (`post_id`);
ALTER TABLE `post_metric_daily`               ADD FOREIGN KEY (`date_key`)           REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_daily`           ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `platform_metric_daily`           ADD FOREIGN KEY (`date_key`)           REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_hourly_metric`          ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `platform_hourly_metric`          ADD FOREIGN KEY (`date_key`)           REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `search_performance_daily`        ADD FOREIGN KEY (`brand_id`)           REFERENCES `brand`           (`brand_id`);
ALTER TABLE `search_performance_daily`        ADD FOREIGN KEY (`date_key`)           REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `keyword_master`                  ADD FOREIGN KEY (`brand_id`)           REFERENCES `brand`           (`brand_id`);
ALTER TABLE `keyword_performance_daily`       ADD FOREIGN KEY (`keyword_id`)         REFERENCES `keyword_master`  (`keyword_id`);
ALTER TABLE `keyword_performance_daily`       ADD FOREIGN KEY (`date_key`)           REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`start_date_key`)     REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`end_date_key`)       REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_yearly_summary`  ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `platform_metric_yearly_summary`  ADD FOREIGN KEY (`start_date_key`)     REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_yearly_summary`  ADD FOREIGN KEY (`end_date_key`)       REFERENCES `date_dimension`  (`date_key`);
ALTER TABLE `platform_metric_monthly_trend`   ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `platform_metric_yearly_trend`    ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `performance_impact_analysis`     ADD FOREIGN KEY (`brand_platform_id`)  REFERENCES `brand_platform`  (`brand_platform_id`);
ALTER TABLE `strategy_recommendation`         ADD FOREIGN KEY (`brand_id`)           REFERENCES `brand`           (`brand_id`);
ALTER TABLE `strategy_recommendation_item`    ADD FOREIGN KEY (`strategy_id`)        REFERENCES `strategy_recommendation` (`strategy_id`);
ALTER TABLE `strategy_recommendation_item`    ADD FOREIGN KEY (`platform_id`)        REFERENCES `platform`        (`platform_id`);


-- ══════════════════════════════════════════════
-- INSERT seed data
-- 순서: 플랫폼 → 브랜드 → 브랜드플랫폼 → 피드백 → 채널성과분석 더미 → 유저샘플
-- ══════════════════════════════════════════════

-- 1. 플랫폼
INSERT IGNORE INTO platform (platform_id, platform_code, platform_name, brand_color, is_active)
VALUES
(1, 'instagram', '인스타그램', '#E1306C', TRUE),
(2, 'facebook',  '페이스북',   '#1877F2', TRUE),
(3, 'naver',     '네이버',     '#03C75A', TRUE),
(4, 'kakao',     '카카오채널', '#FEE500', TRUE);

-- 2. 브랜드
INSERT IGNORE INTO brand (brand_id, user_id, brand_name, service_name, industry_type, location_name, address, phone, profile_image_url, created_at, updated_at) VALUES
(1,  NULL, '어글리베이커리',     'uglybakery',             '카페 / 베이커리',           '망원본점',   '서울 마포구 월드컵로13길 73',           '023382018',   NULL, NOW(), NOW()),
(2,  NULL, '을밀대',             'eulmildae',              '음식점 / 식당',       '염리동본점', '서울 마포구 숭문길 24',                 '027171922',   NULL, NOW(), NOW()),
(3,  NULL, '와드',               'wad_seongsu',            '미용 / 뷰티',          '성수점',     '서울 성동구 서울숲길 51',               '050713898378',NULL, NOW(), NOW()),
(4,  NULL, '선데이클로즈',       'sundayclothes_official', '패션 / 의류',      '을지로점',   '서울 중구 수표로 28',                   '01042276051', NULL, NOW(), NOW()),
(5,  NULL, '산방댁게스트하우스', 'sanbangdaek',            '숙박 / 펜션', 		'사계점',     '제주 서귀포시 안덕면 사계신항로 6',     '01091797585', NULL, NOW(), NOW()),
(6,  NULL, '메인필라테스',       'mainpilates',            '피트니스 / 스포츠',        '잠실본점',   '종로구 자하문로2길 4 4',                '07088616833', NULL, NOW(), NOW()),
(7,  NULL, '슈잇베이킹스튜디오', 'choueat_bakingstudio',   '교육 / 학원',     	'성수동본점', '서울 성동구 상원6길 10',                NULL,          NULL, NOW(), NOW()),
(8,  NULL, '서울SY피부과',       'sydermatology',          '의료 / 병원',      '본점',       '서울 강남구 논현로171길 11',            '025172696',   NULL, NOW(), NOW()),
(9,  NULL, '인덱스숍',           'indexshop.kr',           '소매 / 쇼핑',       '건대점',     '서울 광진구 아차산로 200',              '0221221259',  NULL, NOW(), NOW()),
(10, NULL, '플로애',             '_floae_',                '기타',                   '역삼본점',   '서울 강남구 역삼동 778-6',              '01059156228', NULL, NOW(), NOW()),
(11, NULL, '몽심',               '_creative_mongsim',      '카페 / 베이커리',           '한남대본점', '대전 대덕구 한남로38번길 28',           '01044591014', NULL, NOW(), NOW()),
(12, NULL, '연돈',               'yeondon2014',            '음식점 / 식당',       '중문점',     '제주 서귀포시 색달로 10',               '050713867060',NULL, NOW(), NOW()),
(13, NULL, '헤어웰',             'hairwell',               '미용 / 뷰티',          '서신점',     '전북 전주시 완산구 서신로 104',         '050714182513',NULL, NOW(), NOW()),
(14, NULL, '너겟',               'nugget_min',             '패션 / 의류',      '강릉본점',   '강릉시 원대로8번길 9',                  '07088483542', NULL, NOW(), NOW()),
(15, NULL, '한옥스테이소화',     'tdesign71',              '숙박 / 펜션', 		'황리단길점', '경북 경주시 국당2길 5',                 '01048007205', NULL, NOW(), NOW()),
(16, NULL, '요가베르데',         'yoga__verde',            '피트니스 / 스포츠',        '비자림점',   '제주 제주시 구좌읍 비자림로 1999-6',   '050713936090',NULL, NOW(), NOW()),
(17, NULL, '씨앤씨미술학원',     'suwan_cnc',              '교육 / 학원',     '수완본점',   '광주 광산구 장신로 164',                '0629549711',  NULL, NOW(), NOW()),
(18, NULL, '서산연세치과',       'seosan_yonsei',          '의료 / 병원',      '본점',       '충남 서산시 율지8로 1',                 '0416641616',  NULL, NOW(), NOW()),
(19, NULL, '책방오늘',           'onulbooks_in_seochon',   '소매 / 쇼핑',       '서촌점',     '서울 종로구 자하문로6길 11',            '027337077',   NULL, NOW(), NOW()),
(20, NULL, '삶은감자',           'life_gamja',             '기타',                   '강릉본점',   '강원 강릉시 임영로 197-1',              '050713714429',NULL, NOW(), NOW());

-- 3. 브랜드 플랫폼 (채널 성과 분석 더미가 brand_platform_id 1,2 를 참조하므로 더미보다 선행)
INSERT IGNORE INTO brand_platform (brand_platform_id, brand_id, platform_id, channel_name, channel_url, is_connected, token_status, connected_at, created_at, updated_at)
VALUES
(1, 1, 1, 'uglybakery',       'https://www.instagram.com/uglybakery/', FALSE,  'EXPIRED',  NOW(), NOW(), NOW()),
(2, 1, 2, 'uglybakery',       'https://www.facebook.com/uglybakery',   FALSE, 'EXPIRED', NULL,  NOW(), NOW()),
(3, 1, 3, 'uglybakery_naver', 'https://blog.naver.com/uglybakery',     FALSE, 'EXPIRED', NULL,  NOW(), NOW()),
(4, 1, 4, '@어글리베이커리',   'https://pf.kakao.com/_uglybakery',      FALSE, 'EXPIRED', NULL,  NOW(), NOW());

-- 4. 피드백
INSERT IGNORE INTO `feedback_source` (`source_id`, `author_name`, `created_at`, `original_text`, `platform`) VALUES
(1, '김철수', NOW(), '퇴근길에 들러서 포장했는데 식어도 바삭하고 맛있네요.', 'NAVER'),
(2, '이영희', NOW(), '주차장이 협소해서 조금 불편했지만 친절했어요.', 'KAKAO'),
(3, '정수민', NOW(), '헐 이거 신상이에요?? 당장 먹으러 갑니다', 'INSTAGRAM');

INSERT IGNORE INTO `customer_feedback` (`feedback_id`, `source_id`, `type`, `status`, `ai_status`, `created_at`, `updated_at`) VALUES
(1, 1, 'REVIEW',  'UNRESOLVED', 'IDLE', NOW(), NOW()),
(2, 2, 'REVIEW',  'UNRESOLVED', 'IDLE', NOW(), NOW()),
(3, 3, 'COMMENT', 'UNCHECKED',  'IDLE', NOW(), NOW());

-- 5. 채널 성과 분석 더미
-- date_dimension → content_post → platform_metric_daily → post_metric_daily → performance_impact_analysis
INSERT IGNORE INTO date_dimension (
  date_key, full_date, year_no, half_no, quarter_no, month_no, month_name,
  week_of_year, week_of_month, day_of_month, day_of_week, day_name_kr,
  is_weekend, is_holiday, is_business_day, is_month_start, is_month_end,
  is_year_start, is_year_end, season_code, holiday_name
)
SELECT
  DATE_FORMAT(CURDATE(), '%Y%m%d') + 0,
  CURDATE(),
  YEAR(CURDATE()),
  IF(MONTH(CURDATE()) <= 6, 1, 2),
  QUARTER(CURDATE()),
  MONTH(CURDATE()),
  DATE_FORMAT(CURDATE(), '%M'),
  WEEK(CURDATE(), 3),
  FLOOR((DAY(CURDATE()) - 1) / 7) + 1,
  DAY(CURDATE()),
  WEEKDAY(CURDATE()) + 1,
  ELT(WEEKDAY(CURDATE()) + 1, '월', '화', '수', '목', '금', '토', '일'),
  IF(WEEKDAY(CURDATE()) >= 5, TRUE, FALSE),
  FALSE,
  IF(WEEKDAY(CURDATE()) >= 5, FALSE, TRUE),
  IF(DAY(CURDATE()) = 1, TRUE, FALSE),
  IF(CURDATE() = LAST_DAY(CURDATE()), TRUE, FALSE),
  IF(DAYOFYEAR(CURDATE()) = 1, TRUE, FALSE),
  IF(DAYOFYEAR(CURDATE()) = DAYOFYEAR(LAST_DAY(CONCAT(YEAR(CURDATE()), '-12-01'))), TRUE, FALSE),
  CASE
    WHEN MONTH(CURDATE()) IN (3, 4, 5) THEN 'spring'
    WHEN MONTH(CURDATE()) IN (6, 7, 8) THEN 'summer'
    WHEN MONTH(CURDATE()) IN (9, 10, 11) THEN 'fall'
    ELSE 'winter'
  END,
  NULL
WHERE NOT EXISTS (
  SELECT 1 FROM date_dimension WHERE date_key = DATE_FORMAT(CURDATE(), '%Y%m%d') + 0
);

INSERT IGNORE INTO content_post (
  post_id, brand_platform_id, post_title, post_type, post_body,
  published_at, published_date_key, published_hour, STATUS, created_at, updated_at
)
VALUES
(1, 1, '오픈 이벤트', '이벤트형', '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, HOUR(NOW()), 'published', NOW(), NOW()),
(2, 2, '신메뉴 안내', '공지형',   '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, HOUR(NOW()), 'published', NOW(), NOW());

INSERT IGNORE INTO platform_metric_daily (
  brand_platform_id, date_key, total_views, total_likes, total_comments,
  total_shares, total_reviews, follower_growth, engagement_score, created_at
)
VALUES
(1, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 1200, 130, 20, 8, 5, 12, 15.20, NOW()),
(2, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0,  900,  80, 10, 5, 2,  7, 10.50, NOW());

INSERT IGNORE INTO post_metric_daily (
  post_id, date_key, view_count, like_count, comment_count, share_count,
  follower_gain, review_count, save_count, click_count, created_at
)
VALUES
(1, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 900, 110, 16, 7, 10, 3, 4, 22, NOW()),
(2, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 700,  60,  7, 4,  6, 2, 3, 14, NOW());

INSERT IGNORE INTO performance_impact_analysis (
  brand_platform_id, analysis_period_type, base_year, base_month,
  weekend_effect_score, holiday_effect_score, best_day_of_week,
  worst_day_of_week, best_hour_range, created_at
)
VALUES
(1, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 12.3, 8.5, 6, 2, '18:00-21:00', NOW()),
(2, 'month', YEAR(CURDATE()), MONTH(CURDATE()),  8.1, 6.2, 5, 2, '12:00-14:00', NOW());

-- ===== users, brand 데이터 =====
-- [1] 구글 유저 - 어글리 베이커리 (카페 / 베이커리)
INSERT INTO `users` (
  email, NAME, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, STATUS, ROLE,
  created_at, updated_at
) VALUES (
  'test@social.com', 
  '어글리', 
  '베이커리', 
  'kakao', 
  '1159314857642691679112', -- 22자리 난수화, 
  '01045685213', 
  '어글리베이커리', 
  '카페 / 베이커리', -- 실제 데이터의 '피트니스 / 스포츠' 형식에 맞춤
  'INSTAGRAM',      -- 대문자로 통일
  '023382018',
  '[04014] 서울특별시 마포구 월드컵로13길 73', -- 우편번호 추가 (예시 번호)
  '1층',
  1, 1, 1, 1, 1,    -- event_consent 포함 모든 동의 값을 1로 설정
  1, 
  'ACTIVE', 
  'ROLE_USER',
  NOW(), 
  NOW()
);

-- business_hours (0=월~6=일, 월·화 정기휴무)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(1, 0, 0, NULL,    NULL   ),
(1, 1, 0, NULL,    NULL   ),
(1, 2, 1, '12:00', '21:00'),
(1, 3, 1, '12:00', '21:00'),
(1, 4, 1, '12:00', '21:00'),
(1, 5, 1, '12:00', '21:00'),
(1, 6, 1, '12:00', '21:00');

-- content_settings
INSERT INTO content_settings (
  user_id,
  intro_template, outro_template,
  tone, emoji_level, target_length,
  preferred_sns
) VALUES (
  1,
  '안녕하세요, 망원동 빵대장 어글리베이커리입니다 🍞',
  '오늘도 맛있는 빵과 함께 행복한 하루 되세요 😊',
  '친근한', '적당히', 300,
  'instagram,kakao'
);

-- brand_operation_profile
INSERT INTO brand_operation_profile (
  brand_id,
  open_time, close_time,
  regular_closed_weekday,
  weekend_impact_type, holiday_impact_type,
  peak_business_time, note,
  created_at, updated_at
) VALUES (
  1,
  '12:00:00', '21:00:00',
  1,
  'positive', 'positive',
  '오후 12시~오후 3시',
  '월·화 정기휴무. 빵 예약 불가. 주차 시 망원시장 공영주차장 이용.',
  NOW(), NOW()
);

INSERT INTO inquiry (TYPE, email, title, BODY, content, STATUS, created_at) VALUES
('시스템오류', 'test@social.com', '인스타그램 연동 후 게시물이 업로드되지 않습니다',  '인스타그램 계정 연동은 완료됐는데 게시물 업로드 버튼을 눌러도 아무 반응이 없습니다.',        '인스타그램 계정 연동은 완료됐는데 게시물 업로드 버튼을 눌러도 아무 반응이 없습니다.',        '미처리',   NOW()),
('시스템오류', 'test@social.com', 'AI 콘텐츠 생성 시 오류 메시지가 표시됩니다',        'AI 콘텐츠 생성 버튼 클릭 시 "생성에 실패했습니다" 메시지가 반복적으로 나타납니다.',          'AI 콘텐츠 생성 버튼 클릭 시 "생성에 실패했습니다" 메시지가 반복적으로 나타납니다.',          '미처리',   NOW() - INTERVAL 2 DAY),
('가입·연동', 'test@social.com', '네이버 블로그 연동 후 계정이 바로 해제됩니다',        '네이버 블로그를 연동하면 잠시 후 자동으로 연동이 해제되는 현상이 반복됩니다.',               '네이버 블로그를 연동하면 잠시 후 자동으로 연동이 해제되는 현상이 반복됩니다.',               '처리완료', NOW() - INTERVAL 5 DAY),
('시스템오류', 'test@social.com', '예약 게시 시간이 설정한 시간과 다르게 발행됩니다',   '오후 6시로 예약했는데 오전 6시에 발행됐습니다. 동일한 현상이 3번 반복됐습니다.',            '오후 6시로 예약했는데 오전 6시에 발행됐습니다. 동일한 현상이 3번 반복됐습니다.',            '미처리',   NOW() - INTERVAL 7 DAY);

-- [2] 네이버 유저 - 을밀대 (음식점/식당)
INSERT INTO `users` (
  email, NAME, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, STATUS, ROLE,
  created_at, updated_at
) VALUES (
  'eulmildae@naver.com',
  '을밀대',
  '을밀대',
  'naver',
  '1159314857642691679113', -- 22자리 난수화, 
  '01011112222',
  '을밀대',
  '음식점 / 식당',
  'NAVER',
  '027171922',
  '[04016] 서울 마포구 숭문길 24',
  '염리동본점',
  1, 1, 1, 1, 1,
  1,
  'ACTIVE',
  'ROLE_USER',
  NOW(),
  NOW()
);

-- [3] 구글 유저 - 와드 (미용/뷰티)
INSERT INTO `users` (
  email, NAME, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, STATUS, ROLE,
  created_at, updated_at
) VALUES (
  'wad@google.com',
  '와드',
  '와드',
  'google',
  '1159314857642691679114', -- 22자리 난수화, 
  '01033334444',
  '와드',
  '미용 / 뷰티',
  'INSTAGRAM',
  '050713898378',
  '[04779] 서울 성동구 서울숲길 51',
  '성수점',
  1, 1, 1, 0, 0,
  1,
  'ACTIVE',
  'ROLE_USER',
  NOW(),
  NOW()
);

-- [4] 카카오 유저 - 선데이클로즈 (패션/의류)
INSERT INTO `users` (
  email, NAME, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  preferred_channel, store_phone_number,
  road_addr_part1, addr_detail,
  terms_agreed, privacy_agreed, location_agreed,
  marketing_consent, event_consent,
  signup_completed, STATUS, ROLE,
  created_at, updated_at
) VALUES (
  'sundayclothes@kakao.com',
  '선데이클로즈',
  '선데이클로즈',
  'kakao',
  '1159314857642691679114=5', -- 22자리 난수화, 
  '01055556666',
  '선데이클로즈',
  '패션 / 의류',
  'INSTAGRAM',
  '01042276051',
  '[04554] 서울 중구 수표로 28',
  '을지로점',
  1, 1, 1, 1, 0,
  1,
  'ACTIVE',
  'ROLE_USER',
  NOW(),
  NOW()
);

-- business_hours (2번 유저 - 을밀대, 일요일 휴무)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(2, 0, 1, '11:30', '21:00'),
(2, 1, 1, '11:30', '21:00'),
(2, 2, 1, '11:30', '21:00'),
(2, 3, 1, '11:30', '21:00'),
(2, 4, 1, '11:30', '21:00'),
(2, 5, 1, '11:30', '21:00'),
(2, 6, 0, NULL,    NULL   );

-- business_hours (3번 유저 - 와드, 월요일 휴무)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(3, 0, 0, NULL,    NULL   ),
(3, 1, 1, '10:00', '20:00'),
(3, 2, 1, '10:00', '20:00'),
(3, 3, 1, '10:00', '20:00'),
(3, 4, 1, '10:00', '20:00'),
(3, 5, 1, '10:00', '20:00'),
(3, 6, 1, '10:00', '20:00');

-- business_hours (4번 유저 - 선데이클로즈, 일요일 휴무)
INSERT INTO business_hours (user_id, day_of_week, is_open, open_time, close_time) VALUES
(4, 0, 1, '12:00', '21:00'),
(4, 1, 1, '12:00', '21:00'),
(4, 2, 1, '12:00', '21:00'),
(4, 3, 1, '12:00', '21:00'),
(4, 4, 1, '12:00', '21:00'),
(4, 5, 1, '12:00', '21:00'),
(4, 6, 0, NULL,    NULL   );

-- content_settings (2번 유저 - 을밀대)
INSERT INTO content_settings (
  user_id,
  intro_template, outro_template,
  tone, emoji_level, target_length,
  preferred_sns
) VALUES (
  2,
  '안녕하세요, 마포 냉면 맛집 을밀대입니다 🍜',
  '오늘도 맛있는 식사와 함께 행복한 하루 되세요 😊',
  '친근한', '적당히', 300,
  'naver,kakao'
);

-- content_settings (3번 유저 - 와드)
INSERT INTO content_settings (
  user_id,
  intro_template, outro_template,
  tone, emoji_level, target_length,
  preferred_sns
) VALUES (
  3,
  '안녕하세요, 성수 감성 헤어샵 와드입니다 ✂️',
  '오늘도 예쁘고 당당하게! 다음에 또 만나요 💙',
  '세련된', '적당히', 250,
  'instagram,kakao'
);

-- content_settings (4번 유저 - 선데이클로즈)
INSERT INTO content_settings (
  user_id,
  intro_template, outro_template,
  tone, emoji_level, target_length,
  preferred_sns
) VALUES (
  4,
  '안녕하세요, 을지로 감성 패션 브랜드 선데이클로즈입니다 👗',
  '오늘도 나만의 스타일로 빛나세요 ✨',
  '트렌디한', '많이', 280,
  'instagram'
);

-- inquiry (2번 유저 - 을밀대)
INSERT INTO inquiry (TYPE, email, title, BODY, content, STATUS, created_at) VALUES
('가입·연동', 'eulmildae@naver.com', '네이버 예약 연동 후 알림이 오지 않습니다', '네이버 예약 연동 완료 후 신규 예약이 들어와도 알림이 전혀 오지 않습니다.', '네이버 예약 연동 완료 후 신규 예약이 들어와도 알림이 전혀 오지 않습니다.', '미처리',   NOW()),
('시스템오류', 'eulmildae@naver.com', '메뉴 사진 업로드 시 오류가 발생합니다',    '메뉴 사진 등록 시 "업로드 실패" 메시지가 표시됩니다.',                     '메뉴 사진 등록 시 "업로드 실패" 메시지가 표시됩니다.',                     '처리완료', NOW() - INTERVAL 3 DAY);

-- inquiry (3번 유저 - 와드)
INSERT INTO inquiry (TYPE, email, title, BODY, content, STATUS, created_at) VALUES
('시스템오류', 'wad@google.com', '인스타그램 게시물 예약이 되지 않습니다', '예약 게시 설정 후 지정 시간에 발행되지 않는 문제가 반복됩니다.', '예약 게시 설정 후 지정 시간에 발행되지 않는 문제가 반복됩니다.', '미처리', NOW() - INTERVAL 1 DAY);

-- inquiry (4번 유저 - 선데이클로즈)
INSERT INTO inquiry (TYPE, email, title, BODY, content, STATUS, created_at) VALUES
('콘텐츠 생성', 'sundayclothes@kakao.com', '콘텐츠 자동 생성 시 이미지가 누락됩니다', 'AI 콘텐츠 생성 후 이미지가 첨부되지 않은 상태로 생성됩니다.',    'AI 콘텐츠 생성 후 이미지가 첨부되지 않은 상태로 생성됩니다.',    '처리완료', NOW() - INTERVAL 4 DAY),
('시스템오류', 'sundayclothes@kakao.com', '해시태그 추천 기능이 동작하지 않습니다',   '해시태그 자동 추천 버튼 클릭 시 아무 반응이 없습니다.',           '해시태그 자동 추천 버튼 클릭 시 아무 반응이 없습니다.',           '미처리',   NOW() - INTERVAL 2 DAY);


/* -- 브랜드 아이디 21번 콘텐츠 히스토리 확인 용 테스트 데이터
INSERT INTO brand_platform (brand_id, platform_id, is_connected, token_status, created_at, updated_at)
VALUES 
(21, 1, 0, 'ACTIVE', NOW(), NOW()),
(21, 2, 0, 'ACTIVE', NOW(), NOW()),
(21, 3, 0, 'ACTIVE', NOW(), NOW()),
(21, 4, 0, 'ACTIVE', NOW(), NOW());

INSERT INTO content_post (brand_platform_id, post_title, post_type, post_body, STATUS, created_at, updated_at, published_at)
VALUES 
(5, '딸기 크림 라떼', 'AI생성', '🍓 봄의 시작을 알리는 딸기 크림 라떼가 출시되었습니다! 진한 딸기향과 부드러운 크림의 조화를 느껴보세요.', 'published', NOW(), NOW(), NOW()),
(6, '딸기 크림 라떼', 'AI생성', '봄 신메뉴 딸기 크림 라떼 출시! 매장에서 직접 만나보세요.', 'published', NOW(), NOW(), NOW()),
(7, '딸기 크림 라떼', 'AI생성', '봄 시즌 한정 딸기 크림 라떼를 소개합니다. 신선한 딸기와 크림의 완벽한 조화입니다.', 'published', NOW(), NOW(), NOW());
*/

-- ══════════════════════════════════════════════
-- customerCenter 시드 데이터
-- 순서: admin_user → faq → notice → reply
-- ══════════════════════════════════════════════

-- admin_user (관리자 계정, 비밀번호: admin1234)
INSERT INTO admin_user (login_id, PASSWORD, NAME) VALUES
('admin', 'admin1234', '관리자');

-- faq
INSERT INTO faq (category, question, answer) VALUES
('가입·연동', '소셜 계정 연동은 어떻게 하나요?',                      '마이페이지 > SNS 연동 메뉴에서 연동할 소셜 계정을 선택하고 로그인 절차를 진행하시면 됩니다.'),
('가입·연동', '연동한 계정을 해제하려면 어떻게 하나요?',               '마이페이지 > SNS 연동 메뉴에서 연동된 계정 옆의 해제 버튼을 클릭하시면 됩니다.'),
('콘텐츠 생성', 'AI 콘텐츠 자동 생성은 어떻게 사용하나요?',           '콘텐츠 생성 메뉴에서 업종·톤·길이 등을 설정한 후 생성 버튼을 클릭하시면 AI가 자동으로 콘텐츠를 작성해 드립니다.'),
('콘텐츠 생성', '생성된 콘텐츠를 수정할 수 있나요?',                   '네, 생성 후 편집 화면에서 내용을 자유롭게 수정하실 수 있습니다.'),
('시스템오류', '게시물 업로드 중 오류가 발생할 경우 어떻게 하나요?',   '잠시 후 재시도하시거나, 문제가 지속될 경우 고객센터 문의하기를 통해 오류 화면 캡처와 함께 접수해 주세요.'),
('계정', '회원 탈퇴는 어떻게 하나요?',                                 '마이페이지 > 회원 탈퇴 메뉴에서 진행하실 수 있습니다. 탈퇴 후 데이터는 복구되지 않으니 신중히 결정해 주세요.'),
('계정', '이메일 주소를 변경하고 싶어요.',                             '현재 소셜 로그인 기반으로 운영되어 이메일 직접 변경은 지원되지 않습니다. 소셜 계정의 이메일을 변경하시면 자동으로 반영됩니다.');

-- notice
INSERT INTO notice (category, title, content) VALUES
('공지',    '[공지] 소셜다모아 서비스 오픈 안내',                     '안녕하세요, 소셜다모아입니다.\n소셜다모아 정식 서비스가 오픈되었습니다. 많은 이용 부탁드립니다.\n문의사항은 고객센터를 통해 접수해 주세요.'),
('점검',    '[점검] 4월 정기 시스템 점검 안내 (04/20 02:00~04:00)', '정기 시스템 점검이 진행될 예정입니다.\n- 일시: 2025년 4월 20일(일) 02:00 ~ 04:00\n- 영향: 전 서비스 일시 중단\n점검 중에는 서비스 이용이 불가합니다.'),
('업데이트', '[업데이트] AI 콘텐츠 생성 기능 개선',                   'AI 콘텐츠 생성 품질이 향상되었습니다.\n- 업종별 맞춤 톤 적용 개선\n- 해시태그 추천 정확도 향상\n- 이미지 첨부 안정성 개선\n더 나은 서비스로 찾아뵙겠습니다.');

-- reply (위에서 INSERT된 inquiry id 기준 — 처리완료 건에만 답변 등록)
INSERT INTO reply (inquiry_id, content) VALUES
(3, '안녕하세요, 소셜다모아 고객센터입니다.\n네이버 블로그 연동 해제 문제는 토큰 만료 이슈로 확인되었습니다.\n현재 패치가 완료되었으니 재연동 후 이용 부탁드립니다. 감사합니다.'),
(6, '안녕하세요, 소셜다모아 고객센터입니다.\n메뉴 사진 업로드 오류는 파일 크기 제한(10MB) 초과 시 발생할 수 있습니다.\n파일 크기를 줄여 재시도 부탁드립니다. 감사합니다.'),
(9, '안녕하세요, 소셜다모아 고객센터입니다.\nAI 이미지 첨부 누락 건은 콘텐츠 생성 엔진 업데이트로 수정 완료되었습니다.\n불편을 드려 죄송합니다.');