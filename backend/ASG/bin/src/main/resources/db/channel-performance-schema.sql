CREATE TABLE IF NOT EXISTS brand (
  brand_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand_name VARCHAR(100) NOT NULL,
  service_name VARCHAR(100),
  industry_type VARCHAR(50),
  location_name VARCHAR(150),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS platform (
  platform_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  platform_code VARCHAR(30) UNIQUE NOT NULL,
  platform_name VARCHAR(50) NOT NULL,
  brand_color VARCHAR(20),
  is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS brand_platform (
  brand_platform_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  channel_name VARCHAR(100),
  channel_url VARCHAR(255),
  is_connected BOOLEAN NOT NULL DEFAULT FALSE,
  connected_at DATETIME,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_brand_platform (brand_id, platform_id),
  KEY idx_brand_platform_brand_id (brand_id),
  KEY idx_brand_platform_platform_id (platform_id)
);

CREATE TABLE IF NOT EXISTS content_post (
  post_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand_platform_id BIGINT NOT NULL,
  post_title VARCHAR(200),
  post_type VARCHAR(50),
  post_body TEXT,
  published_at DATETIME,
  published_date_key INT,
  published_hour TINYINT,
  status VARCHAR(30) NOT NULL DEFAULT 'published',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_content_post_brand_platform_id (brand_platform_id),
  KEY idx_content_post_published_date_key (published_date_key)
);

CREATE TABLE IF NOT EXISTS post_metric_daily (
  post_metric_daily_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  date_key INT NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  like_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  share_count INT NOT NULL DEFAULT 0,
  follower_gain INT NOT NULL DEFAULT 0,
  review_count INT NOT NULL DEFAULT 0,
  save_count INT NOT NULL DEFAULT 0,
  click_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_post_metric_daily (post_id, date_key),
  KEY idx_post_metric_daily_date_key (date_key)
);

CREATE TABLE IF NOT EXISTS platform_metric_daily (
  platform_metric_daily_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand_platform_id BIGINT NOT NULL,
  date_key INT NOT NULL,
  total_views INT NOT NULL DEFAULT 0,
  total_likes INT NOT NULL DEFAULT 0,
  total_comments INT NOT NULL DEFAULT 0,
  total_shares INT NOT NULL DEFAULT 0,
  total_reviews INT NOT NULL DEFAULT 0,
  follower_growth INT NOT NULL DEFAULT 0,
  engagement_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_platform_metric_daily (brand_platform_id, date_key),
  KEY idx_platform_metric_daily_date_key (date_key)
);

CREATE TABLE IF NOT EXISTS performance_impact_analysis (
  impact_analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand_platform_id BIGINT NOT NULL,
  analysis_period_type VARCHAR(20) NOT NULL,
  base_year INT NOT NULL,
  base_month TINYINT,
  weekend_effect_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  holiday_effect_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  best_day_of_week TINYINT,
  worst_day_of_week TINYINT,
  best_hour_range VARCHAR(50),
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_impact_analysis (brand_platform_id, analysis_period_type, base_year, base_month)
);