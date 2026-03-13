
-- social_damoa_analytics_final dummy data
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE strategy_recommendation_item;
TRUNCATE TABLE strategy_recommendation;
TRUNCATE TABLE performance_impact_analysis;
TRUNCATE TABLE platform_metric_yearly_trend;
TRUNCATE TABLE platform_metric_monthly_trend;
TRUNCATE TABLE platform_metric_yearly_summary;
TRUNCATE TABLE platform_metric_monthly_summary;
TRUNCATE TABLE keyword_performance_daily;
TRUNCATE TABLE keyword_master;
TRUNCATE TABLE search_performance_daily;
TRUNCATE TABLE platform_hourly_metric;
TRUNCATE TABLE platform_metric_daily;
TRUNCATE TABLE post_metric_daily;
TRUNCATE TABLE content_post;
TRUNCATE TABLE brand_platform;
TRUNCATE TABLE platform;
TRUNCATE TABLE brand_operation_profile;
TRUNCATE TABLE brand;
TRUNCATE TABLE date_dimension;

SET FOREIGN_KEY_CHECKS = 1;

-- 이 부분을 먼저 실행하고 insert 해주세요
-- =========================================================






-- 1. brand
-- =========================================================
INSERT INTO brand (
brand_id, brand_name, service_name, industry_type, location_name, created_at, updated_at
) VALUES
(1, '소셜다모아 카페', '소셜다모아', '카페', '소셜다모아 대전둔산점', '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
(2, '소셜다모아 치킨', '소셜다모아', '외식/치킨', '소셜다모아 유성점', '2026-03-01 09:10:00', '2026-03-01 09:10:00');

-- 2. brand_operation_profile
-- =========================================================
INSERT INTO brand_operation_profile (
operation_profile_id, brand_id, open_time, close_time, regular_closed_weekday,
weekend_impact_type, holiday_impact_type, peak_business_time, note, created_at, updated_at
) VALUES
(1, 1, '09:00:00', '22:00:00', 1, 'positive', 'positive', '12:00-14:00, 18:00-20:00', '주말 방문객 비중이 높은 카페', '2026-03-01 09:20:00', '2026-03-01 09:20:00'),
(2, 2, '11:00:00', '23:00:00', 2, 'positive', 'neutral', '17:00-21:00', '저녁 및 주말 배달 수요가 높음', '2026-03-01 09:25:00', '2026-03-01 09:25:00');

-- 3. platform
-- =========================================================
INSERT INTO platform (
platform_id, platform_code, platform_name, brand_color, is_active
) VALUES
(1, 'instagram', '인스타그램', '#E1306C', TRUE),
(2, 'facebook', '페이스북', '#1877F2', TRUE),
(3, 'naver', '네이버', '#03C75A', TRUE),
(4, 'google', '구글', '#4285F4', TRUE),
(5, 'kakao', '카카오', '#FEE500', TRUE);

-- 4. brand_platform
-- =========================================================
INSERT INTO brand_platform (
brand_platform_id, brand_id, platform_id, channel_name, channel_url,
is_connected, connected_at, created_at, updated_at
) VALUES
(1, 1, 1, '소셜다모아카페_공식', 'https://instagram.com/socialdamoa_cafe', TRUE, '2026-03-01 10:00:00', '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(2, 1, 3, '소셜다모아카페 네이버 플레이스', 'https://place.naver.com/restaurant/1000001', TRUE, '2026-03-01 10:05:00', '2026-03-01 10:05:00', '2026-03-01 10:05:00'),
(3, 1, 4, '소셜다모아카페 구글 비즈니스', 'https://google.com/maps/place/socialdamoa_cafe', TRUE, '2026-03-01 10:10:00', '2026-03-01 10:10:00', '2026-03-01 10:10:00'),
(4, 2, 1, '소셜다모아치킨_공식', 'https://instagram.com/socialdamoa_chicken', TRUE, '2026-03-01 10:15:00', '2026-03-01 10:15:00', '2026-03-01 10:15:00'),
(5, 2, 2, '소셜다모아치킨 페이스북', 'https://facebook.com/socialdamoa_chicken', TRUE, '2026-03-01 10:20:00', '2026-03-01 10:20:00', '2026-03-01 10:20:00'),
(6, 2, 5, '소셜다모아치킨 카카오채널', 'https://pf.kakao.com/socialdamoa_chicken', TRUE, '2026-03-01 10:25:00', '2026-03-01 10:25:00', '2026-03-01 10:25:00');

-- 5. date_dimension
-- 2026-03-01 ~ 2026-03-07
-- =========================================================
INSERT INTO date_dimension (
date_key, full_date, year_no, half_no, quarter_no, month_no, month_name,
week_of_year, week_of_month, day_of_month, day_of_week, day_name_kr,
is_weekend, is_holiday, is_business_day, is_month_start, is_month_end,
is_year_start, is_year_end, season_code, holiday_name
) VALUES
(20260301, '2026-03-01', 2026, 1, 1, 3, 'March', 9, 1, 1, 7, '일', TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, 'spring', NULL),
(20260302, '2026-03-02', 2026, 1, 1, 3, 'March', 10, 1, 2, 1, '월', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, 'spring', NULL),
(20260303, '2026-03-03', 2026, 1, 1, 3, 'March', 10, 1, 3, 2, '화', FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, 'spring', '삼일절 대체휴일'),
(20260304, '2026-03-04', 2026, 1, 1, 3, 'March', 10, 1, 4, 3, '수', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, 'spring', NULL),
(20260305, '2026-03-05', 2026, 1, 1, 3, 'March', 10, 1, 5, 4, '목', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, 'spring', NULL),
(20260306, '2026-03-06', 2026, 1, 1, 3, 'March', 10, 1, 6, 5, '금', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, 'spring', NULL),
(20260307, '2026-03-07', 2026, 1, 1, 3, 'March', 10, 1, 7, 6, '토', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'spring', NULL);

INSERT INTO date_dimension (
  date_key, full_date, year_no, half_no, quarter_no, month_no, month_name,
  week_of_year, week_of_month, day_of_month, day_of_week, day_name_kr,
  is_weekend, is_holiday, is_business_day, is_month_start, is_month_end,
  is_year_start, is_year_end, season_code, holiday_name
) VALUES (
  20260101, '2026-01-01', 2026, 1, 1, 1, 'January',
  1, 1, 1, 4, '목',
  FALSE, TRUE, FALSE, TRUE, FALSE,
  TRUE, FALSE, 'winter', '신정'
);

INSERT INTO date_dimension (
  date_key, full_date, year_no, half_no, quarter_no, month_no, month_name,
  week_of_year, week_of_month, day_of_month, day_of_week, day_name_kr,
  is_weekend, is_holiday, is_business_day, is_month_start, is_month_end,
  is_year_start, is_year_end, season_code, holiday_name
) VALUES (
  20261231, '2026-12-31', 2026, 2, 4, 12, 'December',
  53, 5, 31, 4, '목',
  FALSE, FALSE, TRUE, FALSE, TRUE,
  FALSE, TRUE, 'winter', NULL
);


-- 6. content_post
-- =========================================================
INSERT INTO content_post (
post_id, brand_platform_id, post_title, post_type, post_body,
published_at, published_date_key, published_hour, STATUS, created_at, updated_at
) VALUES
(1, 1, '봄 시즌 딸기 라떼 출시', '이벤트형', '신메뉴 딸기 라떼 출시와 함께 방문 이벤트를 진행합니다.', '2026-03-02 11:00:00', 20260302, 11, 'published', '2026-03-02 10:50:00', '2026-03-02 10:50:00'),
(2, 1, '주말 브런치 할인 안내', '공지형', '토요일 브런치 세트 10% 할인 진행.', '2026-03-06 18:00:00', 20260306, 18, 'published', '2026-03-06 17:50:00', '2026-03-06 17:50:00'),
(3, 4, '치킨+맥주 세트 프로모션', '이벤트형', '금요일 저녁 한정 할인 프로모션.', '2026-03-05 17:00:00', 20260305, 17, 'published', '2026-03-05 16:50:00', '2026-03-05 16:50:00'),
(4, 5, '배달앱 리뷰 감사 이벤트', '리뷰형', '리뷰 작성 고객 대상 쿠폰 증정.', '2026-03-04 14:00:00', 20260304, 14, 'published', '2026-03-04 13:50:00', '2026-03-04 13:50:00');

-- 7. post_metric_daily
-- =========================================================
INSERT INTO post_metric_daily (
post_metric_daily_id, post_id, date_key, view_count, like_count, comment_count,
share_count, follower_gain, review_count, save_count, click_count, created_at
) VALUES
(1, 1, 20260302, 420, 58, 9, 12, 15, 3, 27, 36, '2026-03-02 23:59:00'),
(2, 1, 20260303, 510, 66, 11, 13, 18, 4, 31, 42, '2026-03-03 23:59:00'),
(3, 1, 20260304, 300, 39, 6, 8, 7, 2, 15, 22, '2026-03-04 23:59:00'),

(4, 2, 20260306, 380, 44, 7, 9, 9, 2, 18, 25, '2026-03-06 23:59:00'),
(5, 2, 20260307, 720, 98, 15, 21, 24, 5, 45, 60, '2026-03-07 23:59:00'),

(6, 3, 20260305, 610, 72, 14, 19, 20, 6, 22, 48, '2026-03-05 23:59:00'),
(7, 3, 20260306, 900, 110, 24, 31, 33, 10, 40, 75, '2026-03-06 23:59:00'),

(8, 4, 20260304, 280, 21, 8, 6, 5, 17, 7, 19, '2026-03-04 23:59:00'),
(9, 4, 20260305, 340, 28, 10, 8, 8, 22, 9, 26, '2026-03-05 23:59:00');

-- 8. platform_metric_daily
-- =========================================================
INSERT INTO platform_metric_daily (
platform_metric_daily_id, brand_platform_id, date_key, total_views, total_likes,
total_comments, total_shares, total_reviews, follower_growth, engagement_score, created_at
) VALUES
(1, 1, 20260302, 420, 58, 9, 12, 3, 15, 84.50, '2026-03-02 23:59:00'),
(2, 1, 20260303, 510, 66, 11, 13, 4, 18, 96.80, '2026-03-03 23:59:00'),
(3, 1, 20260304, 300, 39, 6, 8, 2, 7, 54.30, '2026-03-04 23:59:00'),
(4, 1, 20260306, 380, 44, 7, 9, 2, 9, 62.00, '2026-03-06 23:59:00'),
(5, 1, 20260307, 720, 98, 15, 21, 5, 24, 141.50, '2026-03-07 23:59:00'),

(6, 2, 20260302, 120, 8, 2, 1, 6, 0, 18.20, '2026-03-02 23:59:00'),
(7, 2, 20260303, 150, 10, 3, 1, 9, 1, 23.10, '2026-03-03 23:59:00'),
(8, 2, 20260304, 170, 12, 3, 2, 12, 1, 27.40, '2026-03-04 23:59:00'),
(9, 2, 20260305, 160, 9, 2, 1, 11, 0, 21.00, '2026-03-05 23:59:00'),
(10, 2, 20260306, 210, 15, 4, 2, 14, 2, 35.60, '2026-03-06 23:59:00'),
(11, 2, 20260307, 260, 18, 5, 3, 16, 2, 43.30, '2026-03-07 23:59:00'),

(12, 4, 20260305, 610, 72, 14, 19, 6, 20, 118.70, '2026-03-05 23:59:00'),
(13, 4, 20260306, 900, 110, 24, 31, 10, 33, 181.20, '2026-03-06 23:59:00'),
(14, 4, 20260307, 760, 95, 17, 23, 8, 28, 150.40, '2026-03-07 23:59:00'),

(15, 5, 20260304, 280, 21, 8, 6, 17, 5, 49.00, '2026-03-04 23:59:00'),
(16, 5, 20260305, 340, 28, 10, 8, 22, 8, 63.50, '2026-03-05 23:59:00'),
(17, 5, 20260306, 430, 37, 11, 10, 24, 9, 79.20, '2026-03-06 23:59:00'),

(18, 6, 20260305, 190, 12, 4, 2, 0, 11, 29.80, '2026-03-05 23:59:00'),
(19, 6, 20260306, 260, 17, 6, 3, 0, 14, 40.60, '2026-03-06 23:59:00'),
(20, 6, 20260307, 310, 20, 8, 4, 0, 17, 49.10, '2026-03-07 23:59:00');

-- 9. platform_hourly_metric
-- =========================================================
INSERT INTO platform_hourly_metric (
hourly_metric_id, brand_platform_id, date_key, hour_of_day,
view_count, like_count, comment_count, share_count, created_at
) VALUES
(1, 1, 20260307, 10, 120, 18, 2, 4, '2026-03-07 10:59:00'),
(2, 1, 20260307, 12, 180, 22, 4, 6, '2026-03-07 12:59:00'),
(3, 1, 20260307, 18, 240, 35, 6, 8, '2026-03-07 18:59:00'),
(4, 1, 20260307, 20, 180, 23, 3, 3, '2026-03-07 20:59:00'),

(5, 4, 20260306, 17, 220, 25, 5, 6, '2026-03-06 17:59:00'),
(6, 4, 20260306, 19, 310, 44, 9, 12, '2026-03-06 19:59:00'),
(7, 4, 20260306, 21, 270, 31, 7, 9, '2026-03-06 21:59:00'),
(8, 4, 20260306, 22, 100, 10, 3, 4, '2026-03-06 22:59:00'),

(9, 5, 20260305, 14, 90, 8, 2, 2, '2026-03-05 14:59:00'),
(10, 5, 20260305, 18, 130, 11, 4, 3, '2026-03-05 18:59:00'),
(11, 5, 20260305, 20, 120, 9, 4, 3, '2026-03-05 20:59:00'),

(12, 6, 20260307, 11, 70, 4, 1, 1, '2026-03-07 11:59:00'),
(13, 6, 20260307, 17, 110, 8, 3, 2, '2026-03-07 17:59:00'),
(14, 6, 20260307, 20, 130, 8, 4, 1, '2026-03-07 20:59:00');

-- 10. search_performance_daily
-- =========================================================
INSERT INTO search_performance_daily (
search_perf_id, brand_id, date_key, search_engine, search_count, click_count, created_at
) VALUES
(1, 1, 20260302, 'naver', 180, 54, '2026-03-02 23:59:00'),
(2, 1, 20260303, 'naver', 240, 70, '2026-03-03 23:59:00'),
(3, 1, 20260304, 'naver', 210, 61, '2026-03-04 23:59:00'),
(4, 1, 20260305, 'naver', 190, 56, '2026-03-05 23:59:00'),
(5, 1, 20260306, 'naver', 260, 80, '2026-03-06 23:59:00'),
(6, 1, 20260307, 'naver', 320, 102, '2026-03-07 23:59:00'),

(7, 2, 20260304, 'naver', 150, 49, '2026-03-04 23:59:00'),
(8, 2, 20260305, 'naver', 210, 67, '2026-03-05 23:59:00'),
(9, 2, 20260306, 'naver', 300, 102, '2026-03-06 23:59:00'),
(10, 2, 20260307, 'naver', 360, 128, '2026-03-07 23:59:00');

-- 11. keyword_master
-- =========================================================
INSERT INTO keyword_master (
keyword_id, brand_id, keyword_text, keyword_type, created_at
) VALUES
(1, 1, '대전 카페', 'generic', '2026-03-01 11:00:00'),
(2, 1, '딸기 라떼', 'menu', '2026-03-01 11:01:00'),
(3, 1, '소셜다모아 카페', 'brand', '2026-03-01 11:02:00'),
(4, 2, '유성 치킨', 'generic', '2026-03-01 11:03:00'),
(5, 2, '치맥 세트', 'menu', '2026-03-01 11:04:00'),
(6, 2, '소셜다모아 치킨', 'brand', '2026-03-01 11:05:00');

-- 12. keyword_performance_daily
-- =========================================================
INSERT INTO keyword_performance_daily (
keyword_perf_id, keyword_id, date_key, search_count, click_count, rank_no, created_at
) VALUES
(1, 1, 20260302, 90, 24, 7, '2026-03-02 23:59:00'),
(2, 1, 20260303, 110, 31, 5, '2026-03-03 23:59:00'),
(3, 2, 20260303, 160, 49, 3, '2026-03-03 23:59:00'),
(4, 3, 20260303, 130, 40, 4, '2026-03-03 23:59:00'),
(5, 3, 20260306, 150, 46, 3, '2026-03-06 23:59:00'),

(6, 4, 20260305, 120, 37, 6, '2026-03-05 23:59:00'),
(7, 5, 20260306, 180, 62, 3, '2026-03-06 23:59:00'),
(8, 6, 20260306, 140, 48, 4, '2026-03-06 23:59:00'),
(9, 6, 20260307, 190, 71, 2, '2026-03-07 23:59:00');

-- 13. platform_metric_monthly_summary
-- =========================================================
INSERT INTO platform_metric_monthly_summary (
monthly_summary_id, brand_platform_id, year_no, month_no, start_date_key, end_date_key,
total_views, total_likes, total_comments, total_shares, total_reviews, follower_growth,
engagement_score, avg_daily_views, weekend_views, weekday_views, holiday_views,
weekend_ratio, channel_share_ratio, created_at, updated_at
) VALUES
(1, 1, 2026, 3, 20260301, 20260307, 2330, 305, 48, 63, 16, 73, 439.10, 333.00, 720, 1610, 510, 30.90, 42.50, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(2, 2, 2026, 3, 20260301, 20260307, 1070, 72, 19, 10, 68, 6, 168.60, 152.86, 260, 810, 150, 24.30, 19.50, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(3, 4, 2026, 3, 20260301, 20260307, 2270, 277, 55, 73, 24, 81, 450.30, 324.29, 760, 1510, 0, 33.48, 41.40, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(4, 5, 2026, 3, 20260301, 20260307, 1050, 86, 29, 24, 63, 22, 191.70, 150.00, 0, 1050, 0, 0.00, 19.10, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(5, 6, 2026, 3, 20260301, 20260307, 760, 49, 18, 9, 0, 42, 119.50, 108.57, 310, 450, 0, 40.79, 13.90, '2026-03-07 23:59:59', '2026-03-07 23:59:59');

-- 14. platform_metric_yearly_summary
-- =========================================================
INSERT INTO platform_metric_yearly_summary (
yearly_summary_id, brand_platform_id, year_no, start_date_key, end_date_key,
total_views, total_likes, total_comments, total_shares, total_reviews, follower_growth,
engagement_score, avg_monthly_views, weekend_views, weekday_views, holiday_views,
weekend_ratio, channel_share_ratio, created_at, updated_at
) VALUES
(1, 1, 2026, 20260101, 20261231, 2330, 305, 48, 63, 16, 73, 439.10, 2330.00, 720, 1610, 510, 30.90, 42.50, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(2, 2, 2026, 20260101, 20261231, 1070, 72, 19, 10, 68, 6, 168.60, 1070.00, 260, 810, 150, 24.30, 19.50, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(3, 4, 2026, 20260101, 20261231, 2270, 277, 55, 73, 24, 81, 450.30, 2270.00, 760, 1510, 0, 33.48, 41.40, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(4, 5, 2026, 20260101, 20261231, 1050, 86, 29, 24, 63, 22, 191.70, 1050.00, 0, 1050, 0, 0.00, 19.10, '2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(5, 6, 2026, 20260101, 20261231, 760, 49, 18, 9, 0, 42, 119.50, 760.00, 310, 450, 0, 40.79, 13.90, '2026-03-07 23:59:59', '2026-03-07 23:59:59');

-- 15. platform_metric_monthly_trend
-- =========================================================
INSERT INTO platform_metric_monthly_trend (
monthly_trend_id, brand_platform_id, year_no, month_no,
views, likes, comments, shares, followers, created_at
) VALUES
(1, 1, 2026, 3, 2330, 305, 48, 63, 73, '2026-03-07 23:59:59'),
(2, 2, 2026, 3, 1070, 72, 19, 10, 6, '2026-03-07 23:59:59'),
(3, 4, 2026, 3, 2270, 277, 55, 73, 81, '2026-03-07 23:59:59'),
(4, 5, 2026, 3, 1050, 86, 29, 24, 22, '2026-03-07 23:59:59'),
(5, 6, 2026, 3, 760, 49, 18, 9, 42, '2026-03-07 23:59:59');

-- 16. platform_metric_yearly_trend
-- =========================================================
INSERT INTO platform_metric_yearly_trend (
yearly_trend_id, brand_platform_id, year_no,
views, likes, comments, shares, followers, created_at
) VALUES
(1, 1, 2026, 2330, 305, 48, 63, 73, '2026-03-07 23:59:59'),
(2, 2, 2026, 1070, 72, 19, 10, 6, '2026-03-07 23:59:59'),
(3, 4, 2026, 2270, 277, 55, 73, 81, '2026-03-07 23:59:59'),
(4, 5, 2026, 1050, 86, 29, 24, 22, '2026-03-07 23:59:59'),
(5, 6, 2026, 760, 49, 18, 9, 42, '2026-03-07 23:59:59');

-- 17. performance_impact_analysis
-- =========================================================
INSERT INTO performance_impact_analysis (
impact_analysis_id, brand_platform_id, analysis_period_type, base_year, base_month,
weekend_effect_score, holiday_effect_score, best_day_of_week, worst_day_of_week,
best_hour_range, created_at
) VALUES
(1, 1, 'month', 2026, 3, 18.50, 11.20, 6, 3, '18:00-20:00', '2026-03-07 23:59:59'),
(2, 2, 'month', 2026, 3, 7.80, 5.10, 6, 1, '17:00-19:00', '2026-03-07 23:59:59'),
(3, 4, 'month', 2026, 3, 22.30, 4.00, 5, 2, '19:00-21:00', '2026-03-07 23:59:59'),
(4, 5, 'month', 2026, 3, 9.40, 0.00, 5, 3, '18:00-20:00', '2026-03-07 23:59:59'),
(5, 6, 'month', 2026, 3, 15.70, 0.00, 6, 4, '17:00-20:00', '2026-03-07 23:59:59'),

(6, 1, 'year', 2026, NULL, 18.50, 11.20, 6, 3, '18:00-20:00', '2026-03-07 23:59:59'),
(7, 4, 'year', 2026, NULL, 22.30, 4.00, 5, 2, '19:00-21:00', '2026-03-07 23:59:59');

-- 18. strategy_recommendation
-- =========================================================
INSERT INTO strategy_recommendation (
strategy_id, brand_id, period_type, based_on_start_date, based_on_end_date,
summary_text, generated_at, created_at
) VALUES
(1, 1, 'week', '2026-03-01', '2026-03-07',
'인스타그램 채널은 주말과 저녁 시간대 반응이 높았습니다. 네이버 채널은 리뷰 및 검색 유입의 보조 채널 역할이 강하므로 후기 축적형 운영이 효과적입니다.',
'2026-03-07 23:59:59', '2026-03-07 23:59:59'),
(2, 2, 'week', '2026-03-01', '2026-03-07',
'치킨 브랜드는 금요일 저녁~토요일 반응이 가장 높았습니다. 인스타그램은 프로모션 확산, 페이스북은 리뷰/소통, 카카오채널은 재방문 메시지 발송에 적합합니다.',
'2026-03-07 23:59:59', '2026-03-07 23:59:59');

-- 19. strategy_recommendation_item
-- =========================================================
INSERT INTO strategy_recommendation_item (
strategy_item_id, strategy_id, sort_order, recommendation_title,
platform_id, recommended_time_slot, content_type, detail_text, created_at
) VALUES
(1, 1, 1, '핵심 채널 집중 운영', 1, '18:00-20:00', '이벤트형',
'주말 저녁 시간대 인스타그램 이벤트형 게시물이 가장 높은 조회와 저장을 기록했습니다.', '2026-03-07 23:59:59'),
(2, 1, 2, '리뷰/검색 보강', 3, '11:00-13:00', '리뷰형',
'네이버 플레이스는 검색 유입 및 리뷰 전환에 강하므로 방문 후기 유도형 콘텐츠를 강화하세요.', '2026-03-07 23:59:59'),
(3, 1, 3, '브랜드 검색 점유 확대', 4, '상시', '공지형',
'구글 비즈니스 정보 최신화와 사진 업데이트를 병행하면 검색 전환 개선이 가능합니다.', '2026-03-07 23:59:59'),

(4, 2, 1, '프로모션 확산 운영', 1, '19:00-21:00', '이벤트형',
'금요일 저녁 인스타그램 프로모션 게시물이 가장 높은 참여율을 보였습니다.', '2026-03-07 23:59:59'),
(5, 2, 2, '고객 소통 강화', 2, '18:00-20:00', '리뷰형',
'페이스북은 댓글과 리뷰 유도가 우수하므로 고객 후기 재가공 게시물에 적합합니다.', '2026-03-07 23:59:59'),
(6, 2, 3, '재방문 메시지 운영', 5, '17:00-20:00', '공지형',
'카카오채널은 신규 유입보다 재방문 리마인드와 쿠폰형 메시지 전략에 적합합니다.', '2026-03-07 23:59:59');