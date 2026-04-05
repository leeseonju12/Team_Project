## 실행 문제 없음

DROP DATABASE IF EXISTS gather;
CREATE DATABASE gather;
USE gather;

CREATE TABLE `brand` (
  `brand_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '브랜드 ID',
  `brand_name` VARCHAR(100) NOT NULL COMMENT '브랜드명', # 메가커피, 빽다방, 스타벅스, 스텔라떡볶이, 한스브레드, 몽심 등
  `service_name` VARCHAR(100) COMMENT '서비스명',
  `industry_type` VARCHAR(50) COMMENT '업종', # CAFE, RESTAURANT, FAST_FOOD, BAR, BAKERY, DESSERT, HAIR_SALON, NAIL, SPA_MASSAGE, FITNESS, PILATES_YOGA, RETAIL, CLOTHING, LAUNDRY, PET, EDUCATION, ETC
  `location_name` VARCHAR(150) COMMENT '매장명/지점명/위치 표시명', 
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `brand_operation_profile` (
  `operation_profile_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '운영 프로필 ID',
  `brand_id` BIGINT UNIQUE NOT NULL COMMENT '브랜드 ID',
  `open_time` TIME COMMENT '영업 시작 시간',
  `close_time` TIME COMMENT '영업 종료 시간',
  `regular_closed_weekday` TINYINT COMMENT '정기 휴무 요일(1=월 ~ 7=일)',
  `weekend_impact_type` VARCHAR(20) COMMENT 'positive / neutral / negative',
  `holiday_impact_type` VARCHAR(20) COMMENT 'positive / neutral / negative',
  `peak_business_time` VARCHAR(50) COMMENT '피크 영업 시간대',
  `note` TEXT COMMENT '운영 특이사항',
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `platform` (
  `platform_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '플랫폼 ID',
  `platform_code` VARCHAR(30) UNIQUE NOT NULL COMMENT 'instagram, facebook, naver, google, kakao',
  `platform_name` VARCHAR(50) NOT NULL COMMENT '플랫폼명',
  `brand_color` VARCHAR(20) COMMENT '차트/배지 색상 코드',
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부'
);

CREATE TABLE `brand_platform` (
  `brand_platform_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '브랜드-플랫폼 연결 ID',
  `brand_id` BIGINT NOT NULL COMMENT '브랜드 ID',
  `platform_id` BIGINT NOT NULL COMMENT '플랫폼 ID',
  `channel_name` VARCHAR(100) COMMENT '실제 운영 채널명',
  `channel_url` VARCHAR(255) COMMENT '채널 URL',
  `is_connected` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '연동 여부',
  `connected_at` DATETIME COMMENT '연동 일시',
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `date_dimension` (
  `date_key` INT PRIMARY KEY COMMENT 'YYYYMMDD 형식의 날짜 키',
  `full_date` DATE UNIQUE NOT NULL COMMENT '실제 날짜',
  `year_no` INT NOT NULL COMMENT '연도',
  `half_no` TINYINT NOT NULL COMMENT '반기(1,2)',
  `quarter_no` TINYINT NOT NULL COMMENT '분기(1~4)',
  `month_no` TINYINT NOT NULL COMMENT '월(1~12)',
  `month_name` VARCHAR(20) COMMENT '월 이름',
  `week_of_year` TINYINT COMMENT '연중 주차',
  `week_of_month` TINYINT COMMENT '월중 주차',
  `day_of_month` TINYINT NOT NULL COMMENT '일(1~31)',
  `day_of_week` TINYINT NOT NULL COMMENT '요일(1=월 ~ 7=일)',
  `day_name_kr` VARCHAR(10) NOT NULL COMMENT '월, 화, 수, 목, 금, 토, 일',
  `is_weekend` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '주말 여부',
  `is_holiday` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '공휴일 여부',
  `is_business_day` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '영업일 여부',
  `is_month_start` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '월초 여부',
  `is_month_end` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '월말 여부',
  `is_year_start` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '연초 여부',
  `is_year_end` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '연말 여부',
  `season_code` VARCHAR(20) COMMENT 'spring / summer / fall / winter',
  `holiday_name` VARCHAR(100) COMMENT '공휴일명'
);

CREATE TABLE `content_post` (
  `post_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '게시물 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `post_title` VARCHAR(200) COMMENT '게시물 제목',
  `post_type` VARCHAR(50) COMMENT '이벤트형, 공지형, 리뷰형 등',
  `post_body` TEXT COMMENT '게시물 본문',
  `published_at` DATETIME COMMENT '게시 시각',
  `published_date_key` INT COMMENT '게시 날짜 키',
  `published_hour` TINYINT COMMENT '게시 시각(0~23)',
  `status` VARCHAR(30) NOT NULL DEFAULT 'published' COMMENT 'draft, published, archived',
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `post_metric_daily` (
  `post_metric_daily_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '게시물 일별 성과 ID',
  `post_id` BIGINT NOT NULL COMMENT '게시물 ID',
  `date_key` INT NOT NULL COMMENT '날짜 키',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '조회수',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '좋아요 수',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '댓글 수',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '공유 수',
  `follower_gain` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가 수',
  `review_count` INT NOT NULL DEFAULT 0 COMMENT '리뷰 유입 수',
  `save_count` INT NOT NULL DEFAULT 0 COMMENT '저장 수',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '클릭 수',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `platform_metric_daily` (
  `platform_metric_daily_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '플랫폼 일간 성과 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `date_key` INT NOT NULL COMMENT '날짜 키',
  `total_views` INT NOT NULL DEFAULT 0 COMMENT '총 조회수',
  `total_likes` INT NOT NULL DEFAULT 0 COMMENT '총 좋아요',
  `total_comments` INT NOT NULL DEFAULT 0 COMMENT '총 댓글',
  `total_shares` INT NOT NULL DEFAULT 0 COMMENT '총 공유',
  `total_reviews` INT NOT NULL DEFAULT 0 COMMENT '총 리뷰',
  `follower_growth` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가',
  `engagement_score` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '일간 반응 종합 점수',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `platform_hourly_metric` (
  `hourly_metric_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '시간대별 성과 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `date_key` INT NOT NULL COMMENT '날짜 키',
  `hour_of_day` TINYINT NOT NULL COMMENT '0~23시',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '조회수',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '좋아요 수',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '댓글 수',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '공유 수',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `search_performance_daily` (
  `search_perf_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '검색 성과 ID',
  `brand_id` BIGINT NOT NULL COMMENT '브랜드 ID',
  `date_key` INT NOT NULL COMMENT '날짜 키',
  `search_engine` VARCHAR(30) NOT NULL COMMENT 'naver 등',
  `search_count` INT NOT NULL DEFAULT 0 COMMENT '검색수',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '클릭수',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `keyword_master` (
  `keyword_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '키워드 ID',
  `brand_id` BIGINT NOT NULL COMMENT '브랜드 ID',
  `keyword_text` VARCHAR(100) NOT NULL COMMENT '검색 키워드',
  `keyword_type` VARCHAR(30) COMMENT 'brand, menu, campaign, generic',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `keyword_performance_daily` (
  `keyword_perf_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '키워드 성과 ID',
  `keyword_id` BIGINT NOT NULL COMMENT '키워드 ID',
  `date_key` INT NOT NULL COMMENT '날짜 키',
  `search_count` INT NOT NULL DEFAULT 0 COMMENT '검색량',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '클릭량',
  `rank_no` INT COMMENT '인기 순위',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `platform_metric_monthly_summary` (
  `monthly_summary_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '월간 플랫폼 성과 집계 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `year_no` INT NOT NULL COMMENT '집계 연도',
  `month_no` TINYINT NOT NULL COMMENT '집계 월(1~12)',
  `start_date_key` INT NOT NULL COMMENT '집계 시작 날짜 키',
  `end_date_key` INT NOT NULL COMMENT '집계 종료 날짜 키',
  `total_views` INT NOT NULL DEFAULT 0 COMMENT '총 조회수',
  `total_likes` INT NOT NULL DEFAULT 0 COMMENT '총 좋아요',
  `total_comments` INT NOT NULL DEFAULT 0 COMMENT '총 댓글',
  `total_shares` INT NOT NULL DEFAULT 0 COMMENT '총 공유',
  `total_reviews` INT NOT NULL DEFAULT 0 COMMENT '총 리뷰',
  `follower_growth` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가',
  `engagement_score` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '반응 종합 점수',
  `avg_daily_views` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '일평균 조회수',
  `weekend_views` INT NOT NULL DEFAULT 0 COMMENT '주말 조회수',
  `weekday_views` INT NOT NULL DEFAULT 0 COMMENT '평일 조회수',
  `holiday_views` INT NOT NULL DEFAULT 0 COMMENT '공휴일 조회수',
  `weekend_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '주말 조회수 비중(%)',
  `channel_share_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '전체 채널 대비 비중(%)',
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `platform_metric_yearly_summary` (
  `yearly_summary_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '연간 플랫폼 성과 집계 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `year_no` INT NOT NULL COMMENT '집계 연도',
  `start_date_key` INT NOT NULL COMMENT '집계 시작 날짜 키',
  `end_date_key` INT NOT NULL COMMENT '집계 종료 날짜 키',
  `total_views` INT NOT NULL DEFAULT 0 COMMENT '총 조회수',
  `total_likes` INT NOT NULL DEFAULT 0 COMMENT '총 좋아요',
  `total_comments` INT NOT NULL DEFAULT 0 COMMENT '총 댓글',
  `total_shares` INT NOT NULL DEFAULT 0 COMMENT '총 공유',
  `total_reviews` INT NOT NULL DEFAULT 0 COMMENT '총 리뷰',
  `follower_growth` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가',
  `engagement_score` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '반응 종합 점수',
  `avg_monthly_views` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '월평균 조회수',
  `weekend_views` INT NOT NULL DEFAULT 0 COMMENT '주말 조회수',
  `weekday_views` INT NOT NULL DEFAULT 0 COMMENT '평일 조회수',
  `holiday_views` INT NOT NULL DEFAULT 0 COMMENT '공휴일 조회수',
  `weekend_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '주말 조회수 비중(%)',
  `channel_share_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '전체 채널 대비 비중(%)',
  `created_at` DATETIME NOT NULL COMMENT '생성일시',
  `updated_at` DATETIME NOT NULL COMMENT '수정일시'
);

CREATE TABLE `platform_metric_monthly_trend` (
  `monthly_trend_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '월별 추이 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `year_no` INT NOT NULL COMMENT '연도',
  `month_no` TINYINT NOT NULL COMMENT '월(1~12)',
  `views` INT NOT NULL DEFAULT 0 COMMENT '조회수',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '좋아요',
  `comments` INT NOT NULL DEFAULT 0 COMMENT '댓글',
  `shares` INT NOT NULL DEFAULT 0 COMMENT '공유',
  `followers` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `platform_metric_yearly_trend` (
  `yearly_trend_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '연도별 추이 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `year_no` INT NOT NULL COMMENT '연도',
  `views` INT NOT NULL DEFAULT 0 COMMENT '조회수',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '좋아요',
  `comments` INT NOT NULL DEFAULT 0 COMMENT '댓글',
  `shares` INT NOT NULL DEFAULT 0 COMMENT '공유',
  `followers` INT NOT NULL DEFAULT 0 COMMENT '팔로워 증가',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `performance_impact_analysis` (
  `impact_analysis_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '성과 영향 분석 ID',
  `brand_platform_id` BIGINT NOT NULL COMMENT '브랜드 채널 ID',
  `analysis_period_type` VARCHAR(20) NOT NULL COMMENT 'month / year',
  `base_year` INT NOT NULL COMMENT '분석 연도',
  `base_month` TINYINT COMMENT '분석 월',
  `weekend_effect_score` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '주말 효과 점수',
  `holiday_effect_score` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '공휴일 효과 점수',
  `best_day_of_week` TINYINT COMMENT '최고 성과 요일(1=월 ~ 7=일)',
  `worst_day_of_week` TINYINT COMMENT '최저 성과 요일(1=월 ~ 7=일)',
  `best_hour_range` VARCHAR(50) COMMENT '최고 성과 시간대',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `strategy_recommendation` (
  `strategy_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '전략 추천 ID',
  `brand_id` BIGINT NOT NULL COMMENT '브랜드 ID',
  `period_type` VARCHAR(20) NOT NULL COMMENT 'week, month, year',
  `based_on_start_date` DATE NOT NULL COMMENT '분석 시작일',
  `based_on_end_date` DATE NOT NULL COMMENT '분석 종료일',
  `summary_text` TEXT COMMENT '종합 인사이트',
  `generated_at` DATETIME NOT NULL COMMENT '생성 시각',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

CREATE TABLE `strategy_recommendation_item` (
  `strategy_item_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '전략 추천 상세 ID',
  `strategy_id` BIGINT NOT NULL COMMENT '전략 추천 ID',
  `sort_order` INT NOT NULL COMMENT '노출 순서',
  `recommendation_title` VARCHAR(100) NOT NULL COMMENT '핵심 채널, 확장 운영 등',
  `platform_id` BIGINT NOT NULL COMMENT '추천 플랫폼 ID',
  `recommended_time_slot` VARCHAR(50) COMMENT '추천 시간대',
  `content_type` VARCHAR(100) COMMENT '추천 콘텐츠 유형',
  `detail_text` TEXT COMMENT '상세 추천 설명',
  `created_at` DATETIME NOT NULL COMMENT '생성일시'
);

-- ① user 테이블
CREATE TABLE `user` (
  `user_id`     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '유저 ID',
  `email`       VARCHAR(255) UNIQUE NOT NULL      COMMENT '이메일',
  `name`        VARCHAR(100)                      COMMENT '이름',
  `phone`       VARCHAR(20)                       COMMENT '휴대폰 번호',
  `provider`    VARCHAR(30) NOT NULL              COMMENT 'kakao / naver / google',
  `provider_id` VARCHAR(255) NOT NULL             COMMENT 'OAuth 제공자 고유 ID',
  `role`        VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
  `created_at`  DATETIME NOT NULL                 COMMENT '생성일시',
  `updated_at`  DATETIME NOT NULL                 COMMENT '수정일시',
  `deleted_at`  DATETIME                          COMMENT '탈퇴일시 (soft delete)'
);

-- ② content_settings 테이블
CREATE TABLE `content_settings` (
  `settings_id`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '설정 ID',
  `brand_id`          BIGINT UNIQUE NOT NULL            COMMENT '브랜드 ID',
  `tone`              VARCHAR(30)                       COMMENT 'formal / casual / friendly / witty',
  `emoji_level`       VARCHAR(20)                       COMMENT 'none / low / medium / high',
  `char_limit`        INT                               COMMENT '기본 글자수',
  `intro_template`    TEXT                              COMMENT '인트로 양식',
  `outro_template`    TEXT                              COMMENT '아웃트로 양식',
  `default_platforms` VARCHAR(255)                      COMMENT '기본 SNS JSON ex) ["instagram","kakao"]',
  `created_at`        DATETIME NOT NULL                 COMMENT '생성일시',
  `updated_at`        DATETIME NOT NULL                 COMMENT '수정일시'
);

-- ③ brand 컬럼 추가
ALTER TABLE `brand`
  ADD COLUMN `user_id`           BIGINT       COMMENT '유저 ID'        AFTER `brand_id`,
  ADD COLUMN `address`           VARCHAR(255) COMMENT '도로명 주소'     AFTER `location_name`,
  ADD COLUMN `phone`             VARCHAR(20)  COMMENT '가게 전화번호'   AFTER `address`,
  ADD COLUMN `profile_image_url` VARCHAR(500) COMMENT '대표 이미지 URL' AFTER `phone`;

-- ④ brand_platform 컬럼 추가
ALTER TABLE `brand_platform`
  ADD COLUMN `access_token`     TEXT        COMMENT 'SNS 액세스 토큰' AFTER `is_connected`,
  ADD COLUMN `refresh_token`    TEXT        COMMENT 'SNS 리프레시 토큰' AFTER `access_token`,
  ADD COLUMN `token_expires_at` DATETIME    COMMENT '토큰 만료 일시'   AFTER `refresh_token`,
  ADD COLUMN `token_status`     VARCHAR(20) COMMENT 'ACTIVE / EXPIRED' DEFAULT 'ACTIVE' AFTER `token_expires_at`;


CREATE UNIQUE INDEX `brand_platform_index_0` ON `brand_platform` (`brand_id`, `platform_id`);

CREATE INDEX `brand_platform_index_1` ON `brand_platform` (`brand_id`);

CREATE INDEX `brand_platform_index_2` ON `brand_platform` (`platform_id`);

CREATE INDEX `content_post_index_3` ON `content_post` (`brand_platform_id`);

CREATE INDEX `content_post_index_4` ON `content_post` (`published_at`);

CREATE INDEX `content_post_index_5` ON `content_post` (`published_date_key`);

CREATE UNIQUE INDEX `post_metric_daily_index_6` ON `post_metric_daily` (`post_id`, `date_key`);

CREATE INDEX `post_metric_daily_index_7` ON `post_metric_daily` (`date_key`);

CREATE UNIQUE INDEX `platform_metric_daily_index_8` ON `platform_metric_daily` (`brand_platform_id`, `date_key`);

CREATE INDEX `platform_metric_daily_index_9` ON `platform_metric_daily` (`date_key`);

CREATE UNIQUE INDEX `platform_hourly_metric_index_10` ON `platform_hourly_metric` (`brand_platform_id`, `date_key`, `hour_of_day`);

CREATE INDEX `platform_hourly_metric_index_11` ON `platform_hourly_metric` (`date_key`);

CREATE UNIQUE INDEX `search_performance_daily_index_12` ON `search_performance_daily` (`brand_id`, `search_engine`, `date_key`);

CREATE INDEX `search_performance_daily_index_13` ON `search_performance_daily` (`date_key`);

CREATE UNIQUE INDEX `keyword_master_index_14` ON `keyword_master` (`brand_id`, `keyword_text`);

CREATE UNIQUE INDEX `keyword_performance_daily_index_15` ON `keyword_performance_daily` (`keyword_id`, `date_key`);

CREATE INDEX `keyword_performance_daily_index_16` ON `keyword_performance_daily` (`date_key`);

CREATE INDEX `keyword_performance_daily_index_17` ON `keyword_performance_daily` (`rank_no`);

CREATE UNIQUE INDEX `platform_metric_monthly_summary_index_18` ON `platform_metric_monthly_summary` (`brand_platform_id`, `year_no`, `month_no`);

CREATE INDEX `platform_metric_monthly_summary_index_19` ON `platform_metric_monthly_summary` (`year_no`);

CREATE INDEX `platform_metric_monthly_summary_index_20` ON `platform_metric_monthly_summary` (`month_no`);

CREATE UNIQUE INDEX `platform_metric_yearly_summary_index_21` ON `platform_metric_yearly_summary` (`brand_platform_id`, `year_no`);

CREATE INDEX `platform_metric_yearly_summary_index_22` ON `platform_metric_yearly_summary` (`year_no`);

CREATE UNIQUE INDEX `platform_metric_monthly_trend_index_23` ON `platform_metric_monthly_trend` (`brand_platform_id`, `year_no`, `month_no`);

CREATE UNIQUE INDEX `platform_metric_yearly_trend_index_24` ON `platform_metric_yearly_trend` (`brand_platform_id`, `year_no`);

CREATE UNIQUE INDEX `performance_impact_analysis_index_25` ON `performance_impact_analysis` (`brand_platform_id`, `analysis_period_type`, `base_year`, `base_month`);

CREATE INDEX `strategy_recommendation_index_26` ON `strategy_recommendation` (`brand_id`, `period_type`, `based_on_start_date`, `based_on_end_date`, `generated_at`);

CREATE UNIQUE INDEX `strategy_recommendation_item_index_27` ON `strategy_recommendation_item` (`strategy_id`, `sort_order`);

ALTER TABLE `brand` COMMENT = '브랜드/매장 기준 정보';

ALTER TABLE `brand_operation_profile` COMMENT = '사업장 운영 특성 정보';

ALTER TABLE `platform` COMMENT = '플랫폼 마스터';

ALTER TABLE `brand_platform` COMMENT = '브랜드별 운영 채널 정보';

ALTER TABLE `date_dimension` COMMENT = '요일/주말/공휴일 분석을 위한 날짜 차원 테이블';

ALTER TABLE `content_post` COMMENT = '채널별 게시물 원천 데이터';

ALTER TABLE `post_metric_daily` COMMENT = '게시물별 일간 성과 원천 데이터';

ALTER TABLE `platform_metric_daily` COMMENT = '플랫폼 단위 일간 성과 요약';

ALTER TABLE `platform_hourly_metric` COMMENT = '시간대별 성과 데이터';

ALTER TABLE `search_performance_daily` COMMENT = '검색 성과 일간 데이터';

ALTER TABLE `keyword_master` COMMENT = '브랜드별 키워드 마스터';

ALTER TABLE `keyword_performance_daily` COMMENT = '키워드별 일간 성과';

ALTER TABLE `platform_metric_monthly_summary` COMMENT = '월간 KPI 및 주말/평일 영향 집계';

ALTER TABLE `platform_metric_yearly_summary` COMMENT = '연간 KPI 및 주말/평일 영향 집계';

ALTER TABLE `platform_metric_monthly_trend` COMMENT = '월별 추이 차트용 데이터';

ALTER TABLE `platform_metric_yearly_trend` COMMENT = '연도별 추이 차트용 데이터';

ALTER TABLE `performance_impact_analysis` COMMENT = '주말/요일/시간대 영향 분석 결과';

ALTER TABLE `strategy_recommendation` COMMENT = 'AI 전략 추천 헤더';

ALTER TABLE `strategy_recommendation_item` COMMENT = 'AI 전략 추천 카드 상세';

ALTER TABLE `brand_operation_profile` ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

ALTER TABLE `brand_platform` ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

ALTER TABLE `brand_platform` ADD FOREIGN KEY (`platform_id`) REFERENCES `platform` (`platform_id`);

ALTER TABLE `content_post` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `content_post` ADD FOREIGN KEY (`published_date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `post_metric_daily` ADD FOREIGN KEY (`post_id`) REFERENCES `content_post` (`post_id`);

ALTER TABLE `post_metric_daily` ADD FOREIGN KEY (`date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_daily` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `platform_metric_daily` ADD FOREIGN KEY (`date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_hourly_metric` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `platform_hourly_metric` ADD FOREIGN KEY (`date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `search_performance_daily` ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

ALTER TABLE `search_performance_daily` ADD FOREIGN KEY (`date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `keyword_master` ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

ALTER TABLE `keyword_performance_daily` ADD FOREIGN KEY (`keyword_id`) REFERENCES `keyword_master` (`keyword_id`);

ALTER TABLE `keyword_performance_daily` ADD FOREIGN KEY (`date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`start_date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_monthly_summary` ADD FOREIGN KEY (`end_date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_yearly_summary` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `platform_metric_yearly_summary` ADD FOREIGN KEY (`start_date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_yearly_summary` ADD FOREIGN KEY (`end_date_key`) REFERENCES `date_dimension` (`date_key`);

ALTER TABLE `platform_metric_monthly_trend` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `platform_metric_yearly_trend` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `performance_impact_analysis` ADD FOREIGN KEY (`brand_platform_id`) REFERENCES `brand_platform` (`brand_platform_id`);

ALTER TABLE `strategy_recommendation` ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

ALTER TABLE `strategy_recommendation_item` ADD FOREIGN KEY (`strategy_id`) REFERENCES `strategy_recommendation` (`strategy_id`);

ALTER TABLE `strategy_recommendation_item` ADD FOREIGN KEY (`platform_id`) REFERENCES `platform` (`platform_id`);

-- ⑤ FK 추가
ALTER TABLE `brand`
  ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`);

ALTER TABLE `content_settings`
  ADD FOREIGN KEY (`brand_id`) REFERENCES `brand` (`brand_id`);

-- ⑥ content_settings seed (기존 brand_id=1)
SET FOREIGN_KEY_CHECKS = 0;

INSERT IGNORE INTO `content_settings`
  (brand_id, tone, emoji_level, char_limit, intro_template, outro_template, default_platforms, created_at, updated_at)
VALUES
  (1, 'friendly', 'low', 300, '', '', '["instagram","kakao"]', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- seed data
INSERT IGNORE INTO brand (brand_id, brand_name, service_name, industry_type, location_name, created_at, updated_at) VALUES
(1, '오월의종', '오월의종 베이커리', 'CAFE', '합정점', NOW(), NOW()),
(2, '런던베이글뮤지엄', '런던베이글뮤지엄', 'CAFE', '안국점', NOW(), NOW()),
(3, '을밀대', '을밀대 평양냉면', 'RESTAURANT', '마포점', NOW(), NOW()),
(4, '광화문국밥', '광화문국밥', 'RESTAURANT', '광화문점', NOW(), NOW()),
(5, '살롱 드 헤어', '살롱 드 헤어', 'HAIR_SALON', '연남점', NOW(), NOW()),
(6, '문네일살롱', '문네일살롱', 'NAIL', '명동점', NOW(), NOW()),
(7, '레이어드라운지', '레이어드라운지 편집샵', 'CLOTHING', '가로수길점', NOW(), NOW()),
(8, '달고나게스트하우스', '달고나게스트하우스', 'PENSION', '북촌점', NOW(), NOW()),
(9, '버핏그라운드', '버핏그라운드 피트니스', 'FITNESS', '광화문점', NOW(), NOW()),
(10, '그라운드요가', '그라운드요가 스튜디오', 'PILATES_YOGA', '이태원점', NOW(), NOW()),
(11, '와이모어크래프트', '와이모어 크래프트연구소', 'EDUCATION', '성수점', NOW(), NOW()),
(12, '서촌한옥한의원', '서촌 한옥한의원', 'MEDICAL', '서촌점', NOW(), NOW()),
(13, '유어마인드', '유어마인드 독립서점', 'RETAIL', '서촌점', NOW(), NOW()),
(14, '더북소사이어티', '더북소사이어티', 'RETAIL', '한남점', NOW(), NOW()),
(15, '론드리프로젝트', '론드리프로젝트 세탁카페', 'ETC', '해방촌점', NOW(), NOW());

INSERT IGNORE INTO platform (platform_id, platform_code, platform_name, brand_color, is_active)
VALUES
(1, 'instagram', '인스타그램', '#E1306C', TRUE),
(2, 'facebook', '페이스북', '#1877F2', TRUE),
(3, 'naver', '네이버', '#03C75A', TRUE);

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