INSERT IGNORE INTO brand (brand_id, brand_name, service_name, industry_type, location_name, created_at, updated_at)
VALUES (1, '샘플브랜드', '샘플서비스', 'food', '강남점', NOW(), NOW());

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
  published_at, published_date_key, published_hour, status, created_at, updated_at
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