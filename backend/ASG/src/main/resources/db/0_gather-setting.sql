DROP DATABASE IF EXISTS gather;
CREATE DATABASE gather;
USE gather;

-- ══════════════════════════════════════════════
-- users
-- ══════════════════════════════════════════════
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

-- ══════════════════════════════════════════════
-- business_hours
-- ══════════════════════════════════════════════
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

-- ══════════════════════════════════════════════
-- content_settings
-- ══════════════════════════════════════════════
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

-- ══════════════════════════════════════════════
-- brand
-- ══════════════════════════════════════════════
CREATE TABLE `brand` (
  `brand_id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT       NULL         COMMENT '유저 ID (로그인 연동 후 사용)',
  `brand_name`        VARCHAR(100) NOT NULL     COMMENT '브랜드명',
  `industry_type`     VARCHAR(50)  NULL         COMMENT '업종',
  `location_name`     VARCHAR(150) NULL         COMMENT '매장명/지점명',
  `address`           VARCHAR(255) NULL         COMMENT '도로명 주소',
  `phone`             VARCHAR(20)  NULL         COMMENT '가게 전화번호',
  `profile_image_url` VARCHAR(500) NULL         COMMENT '대표 이미지 URL',
  `created_at`        DATETIME     NOT NULL     COMMENT '생성일시',
  `updated_at`        DATETIME     NOT NULL     COMMENT '수정일시',
  PRIMARY KEY (`brand_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='브랜드/매장 기준 정보';

-- ══════════════════════════════════════════════
-- brand_operation_profile
-- ══════════════════════════════════════════════
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

-- ══════════════════════════════════════════════
-- platform
-- ══════════════════════════════════════════════
CREATE TABLE `platform` (
  `platform_id`   BIGINT      NOT NULL AUTO_INCREMENT,
  `platform_code` VARCHAR(30) UNIQUE NOT NULL COMMENT 'instagram / facebook / naver / kakao',
  `platform_name` VARCHAR(50) NOT NULL,
  `brand_color`   VARCHAR(20) NULL,
  `is_active`     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`platform_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='플랫폼 마스터';

-- ══════════════════════════════════════════════
-- brand_platform
-- ══════════════════════════════════════════════
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

-- ══════════════════════════════════════════════
-- feedback
-- ══════════════════════════════════════════════
CREATE TABLE `feedback_source` (
  `source_id`     BIGINT      NOT NULL AUTO_INCREMENT,
  `author_name`   VARCHAR(255) NULL,
  `created_at`    DATETIME(6)  NULL,
  `original_text` TEXT         NULL,
  `platform`      ENUM('FACEBOOK','GOOGLE','INSTAGRAM','KAKAO','NAVER') NULL,
  PRIMARY KEY (`source_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

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

-- ══════════════════════════════════════════════
-- 성과 분석 테이블
-- ══════════════════════════════════════════════
CREATE TABLE `date_dimension` (
  `date_key`        INT      NOT NULL,
  `full_date`       DATE     UNIQUE NOT NULL,
  `year_no`         INT      NOT NULL,
  `half_no`         TINYINT  NOT NULL,
  `quarter_no`      TINYINT  NOT NULL,
  `month_no`        TINYINT  NOT NULL,
  `month_name`      VARCHAR(20)  NULL,
  `week_of_year`    TINYINT  NULL,
  `week_of_month`   TINYINT  NULL,
  `day_of_month`    TINYINT  NOT NULL,
  `day_of_week`     TINYINT  NOT NULL,
  `day_name_kr`     VARCHAR(10) NOT NULL,
  `is_weekend`      BOOLEAN  NOT NULL DEFAULT FALSE,
  `is_holiday`      BOOLEAN  NOT NULL DEFAULT FALSE,
  `is_business_day` BOOLEAN  NOT NULL DEFAULT TRUE,
  `is_month_start`  BOOLEAN  NOT NULL DEFAULT FALSE,
  `is_month_end`    BOOLEAN  NOT NULL DEFAULT FALSE,
  `is_year_start`   BOOLEAN  NOT NULL DEFAULT FALSE,
  `is_year_end`     BOOLEAN  NOT NULL DEFAULT FALSE,
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
  `post_metric_daily_id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id`       BIGINT   NOT NULL,
  `date_key`      INT      NOT NULL,
  `view_count`    INT      NOT NULL DEFAULT 0,
  `like_count`    INT      NOT NULL DEFAULT 0,
  `comment_count` INT      NOT NULL DEFAULT 0,
  `share_count`   INT      NOT NULL DEFAULT 0,
  `follower_gain` INT      NOT NULL DEFAULT 0,
  `review_count`  INT      NOT NULL DEFAULT 0,
  `save_count`    INT      NOT NULL DEFAULT 0,
  `click_count`   INT      NOT NULL DEFAULT 0,
  `created_at`    DATETIME NOT NULL,
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

-- ══════════════════════════════════════════════
-- INDEX
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
-- FK
-- ══════════════════════════════════════════════
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


-- seed data
INSERT IGNORE INTO brand (brand_id, brand_name, service_name, industry_type, location_name, address, created_at, updated_at) VALUES
(1, '어글리베이커리',  'uglybakery',              'CAFE_BAKERY',          '본점', '서울 마포구 망원동',       NOW(), NOW()),
(2, '을밀대',          'eulmildae',               'FOOD_RESTAURANT',      '본점', '서울 마포구 염리동',       NOW(), NOW()),
(3, '와드',            'wad_seongsu',             'BEAUTY_SALON',         '본점', '서울 성동구 서울숲',       NOW(), NOW()),
(4, '선데이클로즈',    'sundayclothes_official',  'FASHION_CLOTHING',     '본점', '서울 중구 을지로',         NOW(), NOW()),
(5, '산방댁게스트하우스', 'sanbangdaek',          'ACCOMMODATION_PENSION','본점', '제주 서귀포시 안덕면',     NOW(), NOW()),
(6, '필라테스서울',    'pilatesseoul',            'FITNESS_SPORTS',       '본점', '서울 송파구 잠실',         NOW(), NOW()),
(7, '잉글리쉬가든',   'englishgarden_21',         'EDUCATION_ACADEMY',    '본점', '강원 춘천시',              NOW(), NOW()),
(8, '서울SY피부과',   'sydermatology',            'MEDICAL_HOSPITAL',     '본점', '서울 강남구 압구정',       NOW(), NOW()),
(9, '인덱스숍',       'indexshop.kr',             'RETAIL_SHOPPING',      '본점', '서울 광진구 건대',         NOW(), NOW()),
(10,'플로애',         '_floae_',                  'ETC',                  '본점', '서울 강남구 역삼동',       NOW(), NOW());

INSERT IGNORE INTO brand (brand_id, brand_name, service_name, industry_type, location_name, address, created_at, updated_at) VALUES
(11, '몽심',          '_creative_mongsim',      'CAFE_BAKERY',          '본점', '대전 대덕구 한남대',        NOW(), NOW()),
(12, '연돈',          'yeondon2014',            'FOOD_RESTAURANT',      '본점', '제주 서귀포시',             NOW(), NOW()),
(13, '헤어웰',        'hairwell',               'BEAUTY_SALON',         '본점', '전북 전주시 서신동',        NOW(), NOW()),
(14, '너겟',          'nugget_min',             'FASHION_CLOTHING',     '본점', '강원 강릉시 원대로',        NOW(), NOW()),
(15, '한옥스테이소화', 'tdesign71',             'ACCOMMODATION_PENSION','본점', '경북 경주시 황남동',        NOW(), NOW()),
(16, '요가베르데',    'yoga__verde',             'FITNESS_SPORTS',       '본점', '제주 제주시 비자림',        NOW(), NOW()),
(17, '씨앤씨미술학원', 'suwan_cnc',             'EDUCATION_ACADEMY',    '본점', '광주 광산구 수완지구',      NOW(), NOW()),
(18, '서산연세치과',  'seosan_yonsei',           'MEDICAL_HOSPITAL',     '본점', '충남 서산시',               NOW(), NOW()),
(19, '책방오늘',      'onulbooks_in_seochon',   'RETAIL_SHOPPING',      '본점', '서울 종로구 서촌',          NOW(), NOW()),
(20, '삶은감자',      'life_gamja',             'ETC',                  '본점', '강원 강릉시 임영로',        NOW(), NOW());

-- users 시드 삽입
INSERT INTO `users` (
  email, NAME, nickname, provider, provider_id,
  contact_phone, company_name, business_category,
  terms_agreed, privacy_agreed, location_agreed,
  signup_completed, STATUS, ROLE,
  created_at, updated_at
) VALUES (
  'test@social.com', '어글리', '베이커리', 'kakao', 'kakao_test_001',
  '010-4568-5246', '어글리베이커리', 'CAFE_BAKERY',
  1, 1, 1,
  1, 'ACTIVE', 'ROLE_USER',
  NOW(), NOW()
);

-- brand의 user_id 연결 (brand_id=1 → users id=1)
UPDATE brand SET user_id = 1 WHERE brand_id = 1;

INSERT IGNORE INTO platform (platform_id, platform_code, platform_name, brand_color, is_active)
VALUES
(1, 'instagram', '인스타그램', '#E1306C', TRUE),
(2, 'facebook', '페이스북', '#1877F2', TRUE),
(3, 'naver', '네이버', '#03C75A', TRUE),
(4, 'kakao', '카카오채널', '#FEE500', TRUE);

INSERT IGNORE INTO brand_platform (brand_platform_id, brand_id, platform_id, channel_name, channel_url, is_connected, connected_at, created_at, updated_at)
VALUES
(1, 1, 1, 'sample_instagram', 'https://instagram.com/sample', TRUE, NOW(), NOW(), NOW()),
(2, 1, 2, 'sample_facebook', 'https://facebook.com/sample', TRUE, NOW(), NOW(), NOW());

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
(2, 2, '신메뉴 안내', '공지형', '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, HOUR(NOW()), 'published', NOW(), NOW());

INSERT IGNORE INTO platform_metric_daily (
  brand_platform_id, date_key, total_views, total_likes, total_comments,
  total_shares, total_reviews, follower_growth, engagement_score, created_at
)
VALUES
(1, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 1200, 130, 20, 8, 5, 12, 15.20, NOW()),
(2, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 900, 80, 10, 5, 2, 7, 10.50, NOW());

INSERT IGNORE INTO post_metric_daily (
  post_id, date_key, view_count, like_count, comment_count, share_count,
  follower_gain, review_count, save_count, click_count, created_at
)
VALUES
(1, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 900, 110, 16, 7, 10, 3, 4, 22, NOW()),
(2, DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 700, 60, 7, 4, 6, 2, 3, 14, NOW());

INSERT IGNORE INTO performance_impact_analysis (
  brand_platform_id, analysis_period_type, base_year, base_month,
  weekend_effect_score, holiday_effect_score, best_day_of_week,
  worst_day_of_week, best_hour_range, created_at
)
VALUES
(1, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 12.3, 8.5, 6, 2, '18:00-21:00', NOW()),
(2, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 8.1, 6.2, 5, 2, '12:00-14:00', NOW());

-- feedback module tables (managed by SQL init, not Hibernate DDL)
CREATE TABLE IF NOT EXISTS `feedback_source` (
    `source_id` BIGINT NOT NULL AUTO_INCREMENT,
    `author_name` VARCHAR(255),
    `created_at` DATETIME(6),
    `original_text` TEXT,
    `platform` ENUM ('FACEBOOK','GOOGLE','INSTAGRAM','KAKAO','NAVER'),
    PRIMARY KEY (`source_id`)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS `customer_feedback` (
    `feedback_id` BIGINT NOT NULL AUTO_INCREMENT,
    `ai_reply` TEXT,
    `ai_status` ENUM ('DONE','IDLE'),
    `created_at` DATETIME(6),
    `sent_reply` TEXT,
    `status` ENUM ('CHECKED','COMPLETED','SENDING','UNCHECKED','UNRESOLVED'),
    `type` ENUM ('COMMENT','REVIEW'),
    `updated_at` DATETIME(6),
    `source_id` BIGINT,
    PRIMARY KEY (`feedback_id`),
    UNIQUE KEY `uk_customer_feedback_source` (`source_id`),
    CONSTRAINT `fk_customer_feedback_source`
      FOREIGN KEY (`source_id`) REFERENCES `feedback_source` (`source_id`)
) ENGINE=INNODB;

-- feedback module seed data
INSERT IGNORE INTO `feedback_source` (`source_id`, `author_name`, `created_at`, `original_text`, `platform`) VALUES
(1, '김철수', NOW(), '퇴근길에 들러서 포장했는데 식어도 바삭하고 맛있네요.', 'NAVER'),
(2, '이영희', NOW(), '주차장이 협소해서 조금 불편했지만 친절했어요.', 'KAKAO'),
(3, '정수민', NOW(), '헐 이거 신상이에요?? 당장 먹으러 갑니다', 'INSTAGRAM');

INSERT IGNORE INTO `customer_feedback` (`feedback_id`, `source_id`, `type`, `status`, `ai_status`, `created_at`, `updated_at`) VALUES
(1, 1, 'REVIEW', 'UNRESOLVED', 'IDLE', NOW(), NOW()),
(2, 2, 'REVIEW', 'UNRESOLVED', 'IDLE', NOW(), NOW()),
(3, 3, 'COMMENT', 'UNCHECKED', 'IDLE', NOW(), NOW());