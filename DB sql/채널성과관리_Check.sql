## 브랜드 전체 KPI 조회
SELECT
    bp.brand_id,
    pms.year_no,
    pms.month_no,
    SUM(pms.total_views)AS total_views,
    SUM(pms.total_likes)AS total_likes,
    SUM(pms.total_comments)AS total_comments,
    SUM(pms.total_shares)AS total_shares,
    SUM(pms.total_reviews)AS total_reviews,
    SUM(pms.follower_growth)AS follower_growth,
    AVG(pms.engagement_score)AS avg_engagement_score
FROM brand_platform bp
JOIN platform_metric_monthly_summary pms
ON bp.brand_platform_id= pms.brand_platform_id
WHERE bp.brand_id=1
AND pms.year_no=2026
AND pms.month_no=3
GROUP BY bp.brand_id, pms.year_no, pms.month_no;

## 채널별 성과 비교 조회
SELECT
    p.platform_name,
    bp.channel_name,
    pms.total_views,
    pms.total_likes,
    pms.total_comments,
    pms.total_shares,
    pms.total_reviews,
    pms.follower_growth,
    pms.channel_share_ratio
FROM brand_platform bp
JOIN platform p
ON bp.platform_id= p.platform_id
JOIN platform_metric_monthly_summary pms
ON bp.brand_platform_id= pms.brand_platform_id
WHERE bp.brand_id=1
AND pms.year_no=2026
AND pms.month_no=3
ORDER BY pms.total_views DESC;

## 기간별 일간 추이 조회
SELECT
    d.full_date,
    SUM(pmd.total_views)AS total_views,
    SUM(pmd.total_likes)AS total_likes,
    SUM(pmd.total_comments)AS total_comments
FROM brand_platform bp
JOIN platform_metric_daily pmd
ON bp.brand_platform_id= pmd.brand_platform_id
JOIN date_dimension d
ON pmd.date_key= d.date_key
WHERE bp.brand_id=1
AND d.full_date BETWEEN'2026-03-01'AND'2026-03-07'
GROUP BY d.full_date
ORDER BY d.full_date;

## 시간대별 베스트 업로드 시간 조회
SELECT
    phm.hour_of_day,
    SUM(phm.view_count)AS total_views,
    SUM(phm.like_count)AS total_likes,
    SUM(phm.comment_count)AS total_comments,
    SUM(phm.share_count)AS total_shares
FROM brand_platform bp
JOIN platform_hourly_metric phm
ON bp.brand_platform_id= phm.brand_platform_id
WHERE bp.brand_id=1
AND phm.date_key BETWEEN 20260301 AND 20260307
GROUP BY phm.hour_of_day
ORDER BY total_views DESC;

## 요일별 성과 조회
SELECT
    d.day_of_week,
    d.day_name_kr,
    SUM(pmd.total_views)AS total_views,
    SUM(pmd.total_likes)AS total_likes,
    SUM(pmd.total_comments)AS total_comments,
    AVG(pmd.engagement_score)AS avg_engagement_score
FROM brand_platform bp
JOIN platform_metric_daily pmd
ON bp.brand_platform_id= pmd.brand_platform_id
JOIN date_dimension d
ON pmd.date_key= d.date_key
WHERE bp.brand_id=1
GROUP BY d.day_of_week, d.day_name_kr
ORDER BY d.day_of_week;

## 주말/평일/공휴일 비교 조회
SELECT
CASE
WHEN d.is_holiday=TRUE THEN'holiday'
WHEN d.is_weekend=TRUE THEN'weekend'
ELSE'weekday'
END AS day_type,
    SUM(pmd.total_views)AS total_views,
    SUM(pmd.total_likes)AS total_likes,
    AVG(pmd.engagement_score)AS avg_engagement_score
FROM brand_platform bp
JOIN platform_metric_daily pmd
ON bp.brand_platform_id= pmd.brand_platform_id
JOIN date_dimension d
ON pmd.date_key= d.date_key
WHERE bp.brand_id=1
GROUP BY
CASE
WHEN d.is_holiday=TRUE THEN'holiday'
WHEN d.is_weekend=TRUE THEN'weekend'
ELSE'weekday'
END;

## 게시물 TOP N 조회
SELECT
    cp.post_id,
    cp.post_title,
    cp.post_type,
    cp.published_at,
    SUM(pmd.view_count)AS total_views,
    SUM(pmd.like_count)AS total_likes,
    SUM(pmd.comment_count)AS total_comments,
    SUM(pmd.share_count)AS total_shares,
    SUM(pmd.save_count)AS total_saves,
    SUM(pmd.click_count)AS total_clicks
FROM content_post cp
JOIN post_metric_daily pmd
ON cp.post_id= pmd.post_id
JOIN brand_platform bp
ON cp.brand_platform_id= bp.brand_platform_id
WHERE bp.brand_id=1
GROUP BY cp.post_id, cp.post_title, cp.post_type, cp.published_at
ORDER BY total_views DESC
LIMIT 10;

## 콘텐츠 유형별 성과 조회
SELECT
    cp.post_type,
COUNT(DISTINCT cp.post_id)AS post_count,
    SUM(pmd.view_count)AS total_views,
    AVG(pmd.view_count)AS avg_views_per_day,
    SUM(pmd.like_count)AS total_likes,
    SUM(pmd.comment_count)AS total_comments
FROM content_post cp
JOIN post_metric_daily pmd
ON cp.post_id= pmd.post_id
JOIN brand_platform bp
ON cp.brand_platform_id= bp.brand_platform_id
WHERE bp.brand_id=1
GROUP BY cp.post_type
ORDER BY total_views DESC;

## 키워드 성과 TOP 조회
SELECT
    km.keyword_text,
    km.keyword_type,
    SUM(kpd.search_count)AS total_search_count,
    SUM(kpd.click_count)AS total_click_count,
    AVG(kpd.rank_no)AS avg_rank_no
FROM keyword_master km
JOIN keyword_performance_daily kpd
ON km.keyword_id= kpd.keyword_id
WHERE km.brand_id=1
GROUP BY km.keyword_id, km.keyword_text, km.keyword_type
ORDER BY total_search_count DESC
LIMIT 10;

## 최신 전략 추천 조회
SELECT
    sr.strategy_id,
    sr.period_type,
    sr.based_on_start_date,
    sr.based_on_end_date,
    sr.summary_text,
    sr.generated_at
FROM strategy_recommendation sr
WHERE sr.brand_id=1
ORDER BY sr.generated_atDESC
LIMIT 1;

## 상세 카드 조회
SELECT
    sri.sort_order,
    sri.recommendation_title,
    p.platform_name,
    sri.recommended_time_slot,
    sri.content_type,
    sri.detail_text
FROM strategy_recommendation_item sri
JOIN platform p
ON sri.platform_id= p.platform_id
WHERE sri.strategy_id=1
ORDER BY sri.sort_order;