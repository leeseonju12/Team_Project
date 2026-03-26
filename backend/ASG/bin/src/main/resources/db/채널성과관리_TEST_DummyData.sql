-- SQLyog 호환: 채널 성과 분석 더미 데이터(단일 파일)
-- 기간: 2025-01-01 ~ 2026-03-07 (431일)
-- 충돌 방지를 위해 전용 스키마를 사용합니다.

CREATE DATABASE IF NOT EXISTS channel_perf_dummy CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE channel_perf_dummy;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS strategy_recommendation_item;
DROP TABLE IF EXISTS performance_impact_analysis;
DROP TABLE IF EXISTS platform_metric_yearly_trend;
DROP TABLE IF EXISTS platform_metric_monthly_trend;
DROP TABLE IF EXISTS platform_metric_yearly_summary;
DROP TABLE IF EXISTS platform_metric_monthly_summary;
DROP TABLE IF EXISTS search_performance_daily;
DROP TABLE IF EXISTS platform_hourly_metric;
DROP TABLE IF EXISTS keyword_performance_daily;
DROP TABLE IF EXISTS platform_metric_daily;
DROP TABLE IF EXISTS date_dimension;
DROP TABLE IF EXISTS keyword_master;
DROP TABLE IF EXISTS platform_master;
DROP TABLE IF EXISTS brand_master;
DROP TABLE IF EXISTS season_factor;
DROP TABLE IF EXISTS weekday_factor;
DROP TABLE IF EXISTS holiday_calendar;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE brand_master (brand_id INT PRIMARY KEY, brand_name VARCHAR(100));
CREATE TABLE platform_master (platform_id INT PRIMARY KEY, CODE VARCHAR(30), NAME VARCHAR(50), color_hex VARCHAR(10), view_min INT, view_max INT, like_min INT, like_max INT, engagement_avg DECIMAL(6,1));
CREATE TABLE keyword_master (keyword_id INT PRIMARY KEY, keyword_text VARCHAR(100), keyword_type VARCHAR(20));
CREATE TABLE date_dimension (date_key INT PRIMARY KEY, full_date DATE, year_no INT, month_no INT, day_of_week INT, is_weekend TINYINT, season_code VARCHAR(10));
CREATE TABLE platform_metric_daily (id BIGINT AUTO_INCREMENT PRIMARY KEY, brand_id INT, platform_id INT, date_key INT, total_views INT, total_likes INT, total_comments INT, total_shares INT, follower_growth INT, engagement_score DECIMAL(8,2));
CREATE TABLE keyword_performance_daily (id BIGINT AUTO_INCREMENT PRIMARY KEY, date_key INT, keyword_id INT, search_count INT, click_count INT, avg_rank DECIMAL(4,1));
CREATE TABLE platform_hourly_metric (id BIGINT AUTO_INCREMENT PRIMARY KEY, date_key INT, platform_id INT, hour_no INT, avg_views INT);
CREATE TABLE search_performance_daily (id BIGINT AUTO_INCREMENT PRIMARY KEY, date_key INT, ENGINE VARCHAR(20), search_count INT, click_count INT);
CREATE TABLE platform_metric_monthly_summary (id BIGINT AUTO_INCREMENT PRIMARY KEY, year_no INT, month_no INT, platform_id INT, total_views INT, weekend_ratio DECIMAL(5,2), channel_share_ratio DECIMAL(5,2));
CREATE TABLE platform_metric_yearly_summary (id BIGINT AUTO_INCREMENT PRIMARY KEY, year_no INT, platform_id INT, total_views INT, weekend_ratio DECIMAL(5,2), channel_share_ratio DECIMAL(5,2));
CREATE TABLE platform_metric_monthly_trend (id BIGINT AUTO_INCREMENT PRIMARY KEY, year_no INT, month_no INT, platform_id INT, trend_views INT);
CREATE TABLE platform_metric_yearly_trend (id BIGINT AUTO_INCREMENT PRIMARY KEY, year_no INT, platform_id INT, trend_views INT);
CREATE TABLE performance_impact_analysis (analysis_id INT PRIMARY KEY, channel_name VARCHAR(50), impact_desc VARCHAR(120), uplift_pct DECIMAL(5,2));
CREATE TABLE strategy_recommendation_item (item_id INT PRIMARY KEY, title VARCHAR(100), detail_text VARCHAR(300));
CREATE TABLE season_factor (season_code VARCHAR(10) PRIMARY KEY, coef DECIMAL(4,2));
CREATE TABLE weekday_factor (weekday_no INT PRIMARY KEY, coef DECIMAL(4,2));
CREATE TABLE holiday_calendar (holiday_date DATE PRIMARY KEY, holiday_name VARCHAR(60), coef DECIMAL(4,2));

INSERT INTO brand_master VALUES (1,'소셜다모아 카페');
INSERT INTO platform_master VALUES
(1,'instagram','인스타그램','#E1306C',275,817,40,126,194.4),
(2,'naver','네이버 블로그','#03C75A',183,472,13,38,86.3),
(3,'google','구글 지도','#EA4335',94,355,5,18,59.1),
(4,'facebook','페이스북','#1877F2',131,395,7,28,59.0),
(5,'kakao','카카오채널','#F9C000',128,381,7,19,32.4);
INSERT INTO keyword_master VALUES
(1,'강남 카페','generic'),(2,'홍대 카페','generic'),(3,'디카페인 라떼','menu'),(4,'브런치 맛집','menu'),
(5,'수제 티라미수','menu'),(6,'소셜다모아 카페','brand'),(7,'소셜다모아 디저트','brand'),(8,'소셜다모아 예약','brand');
INSERT INTO season_factor VALUES ('spring',1.15),('summer',1.05),('fall',1.12),('winter',0.88);
INSERT INTO weekday_factor VALUES (0,1.25),(1,1.00),(2,1.00),(3,1.00),(4,1.00),(5,1.20),(6,1.35);
INSERT INTO holiday_calendar VALUES ('2025-12-25','크리스마스',1.18),('2026-01-01','신정',1.18);

INSERT INTO date_dimension (date_key, full_date, year_no, month_no, day_of_week, is_weekend, season_code)
WITH RECURSIVE d AS (
  SELECT DATE('2025-01-01') AS dt
  UNION ALL
  SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM d WHERE dt < '2026-03-07'
)
SELECT DATE_FORMAT(dt,'%Y%m%d')+0, dt, YEAR(dt), MONTH(dt), DAYOFWEEK(dt)-1,
       CASE WHEN DAYOFWEEK(dt) IN (1,7) THEN 1 ELSE 0 END,
       CASE WHEN MONTH(dt) BETWEEN 3 AND 5 THEN 'spring' WHEN MONTH(dt) BETWEEN 6 AND 8 THEN 'summer' WHEN MONTH(dt) BETWEEN 9 AND 11 THEN 'fall' ELSE 'winter' END
FROM d;

INSERT INTO platform_metric_daily (brand_id, platform_id, date_key, total_views, total_likes, total_comments, total_shares, follower_growth, engagement_score)
SELECT 1, p.platform_id, dd.date_key,
ROUND((p.view_min + (p.view_max-p.view_min) * ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2)) * sf.coef * wf.coef),
ROUND((p.like_min + (p.like_max-p.like_min) * ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2)) * wf.coef),
ROUND((p.like_min + (p.like_max-p.like_min) * ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2)) * 0.24),
ROUND((p.like_min + (p.like_max-p.like_min) * ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2)) * 0.11),
ROUND((p.like_min + (p.like_max-p.like_min) * ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2)) * 0.35),
ROUND(p.engagement_avg + ((SIN((ROW_NUMBER() OVER (ORDER BY dd.date_key)+p.platform_id*11)/9)+1)/2) * 16, 2)
FROM date_dimension dd
JOIN platform_master p
JOIN season_factor sf ON sf.season_code = dd.season_code
JOIN weekday_factor wf ON wf.weekday_no = dd.day_of_week;

INSERT INTO keyword_performance_daily (date_key, keyword_id, search_count, click_count, avg_rank)
SELECT dd.date_key, km.keyword_id,
ROUND((CASE km.keyword_type WHEN 'generic' THEN 56 WHEN 'menu' THEN 66 ELSE 57 END) +
((CASE km.keyword_type WHEN 'generic' THEN 206 WHEN 'menu' THEN 273 ELSE 260 END) - (CASE km.keyword_type WHEN 'generic' THEN 56 WHEN 'menu' THEN 66 ELSE 57 END)) * ((COS((ROW_NUMBER() OVER (ORDER BY dd.date_key)+km.keyword_id*13)/11)+1)/2)),
ROUND(((CASE km.keyword_type WHEN 'generic' THEN 56 WHEN 'menu' THEN 66 ELSE 57 END) +
((CASE km.keyword_type WHEN 'generic' THEN 206 WHEN 'menu' THEN 273 ELSE 260 END) - (CASE km.keyword_type WHEN 'generic' THEN 56 WHEN 'menu' THEN 66 ELSE 57 END)) * ((COS((ROW_NUMBER() OVER (ORDER BY dd.date_key)+km.keyword_id*13)/11)+1)/2)) * (0.32 + (km.keyword_id % 3)*0.04)),
ROUND(4 + (((COS((ROW_NUMBER() OVER (ORDER BY dd.date_key)+km.keyword_id*13)/11)+1)/2)*1.2), 1)
FROM date_dimension dd JOIN keyword_master km;

INSERT INTO platform_hourly_metric (date_key, platform_id, hour_no, avg_views)
SELECT dd.date_key, p.platform_id, h.hour_no,
ROUND((CASE h.hour_no WHEN 10 THEN 32 WHEN 11 THEN 39 WHEN 12 THEN 51 WHEN 13 THEN 42 WHEN 14 THEN 34 WHEN 15 THEN 31 WHEN 16 THEN 36 WHEN 17 THEN 43 WHEN 18 THEN 47 ELSE 38 END)
* (CASE p.platform_id WHEN 1 THEN 1.15 ELSE 0.95 END))
FROM (SELECT * FROM date_dimension WHERE MOD(DATEDIFF(full_date,'2025-01-01'),3)=0) dd
JOIN (SELECT 1 AS platform_id UNION ALL SELECT 2) p
JOIN (SELECT 10 hour_no UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19) h;

INSERT INTO search_performance_daily (date_key, ENGINE, search_count, click_count)
SELECT dd.date_key, 'naver', ROUND(130 + (477-130)*((SIN(ROW_NUMBER() OVER (ORDER BY dd.date_key)/10)+1)/2)), ROUND(22 + (274-22)*((SIN(ROW_NUMBER() OVER (ORDER BY dd.date_key)/10)+1)/2)) FROM date_dimension dd
UNION ALL
SELECT dd.date_key, 'google', ROUND(102 + (328-102)*((COS(ROW_NUMBER() OVER (ORDER BY dd.date_key)/12)+1)/2)), ROUND(19 + (189-19)*((COS(ROW_NUMBER() OVER (ORDER BY dd.date_key)/12)+1)/2)) FROM date_dimension dd;

INSERT INTO platform_metric_monthly_summary (year_no, month_no, platform_id, total_views, weekend_ratio, channel_share_ratio)
SELECT YEAR(full_date), MONTH(full_date), platform_id, SUM(total_views), ROUND(28.7 + platform_id*2.1,1), ROUND(13.6 + platform_id*4.4,1)
FROM platform_metric_daily pmd JOIN date_dimension dd ON dd.date_key=pmd.date_key GROUP BY YEAR(full_date), MONTH(full_date), platform_id;

INSERT INTO platform_metric_yearly_summary (year_no, platform_id, total_views, weekend_ratio, channel_share_ratio)
SELECT YEAR(full_date), platform_id, SUM(total_views), ROUND(29.1 + platform_id*1.8,1), ROUND(14.1 + platform_id*4.3,1)
FROM platform_metric_daily pmd JOIN date_dimension dd ON dd.date_key=pmd.date_key GROUP BY YEAR(full_date), platform_id;

INSERT INTO platform_metric_monthly_trend (year_no, month_no, platform_id, trend_views)
SELECT year_no, month_no, platform_id, total_views FROM platform_metric_monthly_summary;
INSERT INTO platform_metric_yearly_trend (year_no, platform_id, trend_views)
SELECT year_no, platform_id, total_views FROM platform_metric_yearly_summary;

INSERT INTO performance_impact_analysis VALUES
(1,'인스타그램','주말 집중 운영 시 도달률 상승',18.2),(2,'네이버 블로그','브랜드 키워드 강화로 클릭 증가',14.7),(3,'구글 지도','리뷰 유도 문구가 전환 개선',11.4),
(4,'페이스북','금요일 카드뉴스 반응 우수',9.8),(5,'카카오채널','재방문 쿠폰 메시지 효과',8.9),(6,'전체','점심/저녁 피크 시간대 반응 우세',12.1),(7,'전체','공휴일 프로모션 전환 개선',10.6);

INSERT INTO strategy_recommendation_item VALUES
(1,'핵심 채널 집중','인스타그램·네이버에 제작 리소스를 우선 배정'),
(2,'주말 슬롯 강화','토/일 게시 빈도와 예산을 증액'),
(3,'브랜드 키워드 확대','브랜드성 검색어를 중심으로 콘텐츠 제목 통일'),
(4,'점심 피크 공략','11~13시 업로드 비중을 확대'),
(5,'리뷰 전환 캠페인','구글 지도/네이버 리뷰 이벤트를 상시 운영');