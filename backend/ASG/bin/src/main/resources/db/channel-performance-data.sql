-- 채널 성과 분석 테스트용 더미 데이터
-- 실행 시점 기준 최근 7일 데이터를 생성합니다.

INSERT IGNORE INTO brand (brand_id, brand_name, service_name, industry_type, location_name, created_at, updated_at)
VALUES
(1, '샘플브랜드', '샘플서비스', 'food', '강남점', NOW(), NOW()),
(2, '데모브랜드', '데모서비스', 'cafe', '홍대점', NOW(), NOW());

INSERT IGNORE INTO platform (platform_id, platform_code, platform_name, brand_color, is_active)
VALUES
(1, 'instagram', '인스타그램', '#E1306C', TRUE),
(2, 'facebook', '페이스북', '#1877F2', TRUE),
(3, 'naver', '네이버', '#03C75A', TRUE),
(4, 'google', '구글', '#EA4335', TRUE),
(5, 'kakao', '카카오', '#FEE500', TRUE);

INSERT IGNORE INTO brand_platform (brand_platform_id, brand_id, platform_id, channel_name, channel_url, is_connected, connected_at, created_at, updated_at)
VALUES
(1, 1, 1, 'sample_instagram', 'https://instagram.com/sample', TRUE, NOW(), NOW(), NOW()),
(2, 1, 2, 'sample_facebook', 'https://facebook.com/sample', TRUE, NOW(), NOW(), NOW()),
(3, 1, 3, 'sample_naver', 'https://map.naver.com/sample', TRUE, NOW(), NOW(), NOW());

-- 최근 7일 date_dimension 보장
INSERT IGNORE INTO date_dimension (
  date_key, full_date, year_no, half_no, quarter_no, month_no, month_name,
  week_of_year, week_of_month, day_of_month, day_of_week, day_name_kr,
  is_weekend, is_holiday, is_business_day, is_month_start, is_month_end,
  is_year_start, is_year_end, season_code, holiday_name
)
SELECT
  DATE_FORMAT(d, '%Y%m%d') + 0,
  d,
  YEAR(d),
  IF(MONTH(d) <= 6, 1, 2),
  QUARTER(d),
  MONTH(d),
  DATE_FORMAT(d, '%M'),
  WEEK(d, 3),
  FLOOR((DAY(d) - 1) / 7) + 1,
  DAY(d),
  WEEKDAY(d) + 1,
  ELT(WEEKDAY(d) + 1, '월', '화', '수', '목', '금', '토', '일'),
  IF(WEEKDAY(d) >= 5, TRUE, FALSE),
  FALSE,
  IF(WEEKDAY(d) >= 5, FALSE, TRUE),
  IF(DAY(d) = 1, TRUE, FALSE),
  IF(d = LAST_DAY(d), TRUE, FALSE),
  IF(DAYOFYEAR(d) = 1, TRUE, FALSE),
  IF(DAYOFYEAR(d) = DAYOFYEAR(LAST_DAY(CONCAT(YEAR(d), '-12-01'))), TRUE, FALSE),
  CASE
    WHEN MONTH(d) IN (3, 4, 5) THEN 'spring'
    WHEN MONTH(d) IN (6, 7, 8) THEN 'summer'
    WHEN MONTH(d) IN (9, 10, 11) THEN 'fall'
    ELSE 'winter'
  END,
  NULL
FROM (
  SELECT CURDATE() AS d UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY) UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY) UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY) UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY) UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY) UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY)
) days;

INSERT IGNORE INTO content_post (
  post_id, brand_platform_id, post_title, post_type, post_body,
  published_at, published_date_key, published_hour, status, created_at, updated_at
)
VALUES
(1, 1, '오픈 이벤트', '이벤트형', '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 10, 'published', NOW(), NOW()),
(2, 2, '신메뉴 안내', '공지형', '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 12, 'published', NOW(), NOW()),
(3, 3, '리뷰 감사 이벤트', '리뷰형', '샘플 게시물입니다', NOW(), DATE_FORMAT(CURDATE(), '%Y%m%d') + 0, 18, 'published', NOW(), NOW());

-- 최근 7일 플랫폼 성과
INSERT IGNORE INTO platform_metric_daily (
  brand_platform_id, date_key, total_views, total_likes, total_comments,
  total_shares, total_reviews, follower_growth, engagement_score, created_at
)
SELECT 1, DATE_FORMAT(d, '%Y%m%d') + 0,
       1000 + (n * 70), 110 + (n * 6), 20 + n, 8 + FLOOR(n/2), 4 + FLOOR(n/3), 10 + n,
       12.00 + n, NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

INSERT IGNORE INTO platform_metric_daily (
  brand_platform_id, date_key, total_views, total_likes, total_comments,
  total_shares, total_reviews, follower_growth, engagement_score, created_at
)
SELECT 2, DATE_FORMAT(d, '%Y%m%d') + 0,
       800 + (n * 55), 85 + (n * 4), 12 + n, 5 + FLOOR(n/2), 2 + FLOOR(n/3), 6 + n,
       9.50 + (n * 0.8), NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

INSERT IGNORE INTO platform_metric_daily (
  brand_platform_id, date_key, total_views, total_likes, total_comments,
  total_shares, total_reviews, follower_growth, engagement_score, created_at
)
SELECT 3, DATE_FORMAT(d, '%Y%m%d') + 0,
       600 + (n * 45), 60 + (n * 3), 8 + n, 4 + FLOOR(n/2), 1 + FLOOR(n/3), 4 + n,
       7.80 + (n * 0.6), NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

-- 최근 7일 게시물 성과
INSERT IGNORE INTO post_metric_daily (
  post_id, date_key, view_count, like_count, comment_count, share_count,
  follower_gain, review_count, save_count, click_count, created_at
)
SELECT 1, DATE_FORMAT(d, '%Y%m%d') + 0,
       900 + (n * 60), 100 + (n * 5), 15 + n, 7 + FLOOR(n/2),
       8 + n, 2 + FLOOR(n/3), 4 + FLOOR(n/2), 20 + n, NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

INSERT IGNORE INTO post_metric_daily (
  post_id, date_key, view_count, like_count, comment_count, share_count,
  follower_gain, review_count, save_count, click_count, created_at
)
SELECT 2, DATE_FORMAT(d, '%Y%m%d') + 0,
       700 + (n * 45), 70 + (n * 4), 10 + n, 5 + FLOOR(n/2),
       5 + n, 1 + FLOOR(n/3), 3 + FLOOR(n/2), 14 + n, NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

INSERT IGNORE INTO post_metric_daily (
  post_id, date_key, view_count, like_count, comment_count, share_count,
  follower_gain, review_count, save_count, click_count, created_at
)
SELECT 3, DATE_FORMAT(d, '%Y%m%d') + 0,
       500 + (n * 35), 45 + (n * 3), 7 + n, 3 + FLOOR(n/2),
       3 + n, 1 + FLOOR(n/4), 2 + FLOOR(n/2), 10 + n, NOW()
FROM (
  SELECT CURDATE() AS d, 0 AS n UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5 UNION ALL
  SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 6
) x;

INSERT IGNORE INTO performance_impact_analysis (
  brand_platform_id, analysis_period_type, base_year, base_month,
  weekend_effect_score, holiday_effect_score, best_day_of_week,
  worst_day_of_week, best_hour_range, created_at
)
VALUES
(1, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 12.3, 8.5, 6, 2, '18:00-21:00', NOW()),
(2, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 8.1, 6.2, 5, 2, '12:00-14:00', NOW()),
(3, 'month', YEAR(CURDATE()), MONTH(CURDATE()), 6.8, 4.4, 4, 1, '11:00-13:00', NOW());