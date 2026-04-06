USE `gather`;

CREATE TABLE upload_guide_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_code VARCHAR(30) COMMENT '플랫폼 코드 (예: INSTAGRAM)',
    industry_code VARCHAR(10) COMMENT '산업군 코드 (향후 참조용 논리적 FK)',
    recommended_time VARCHAR(20) COMMENT '추천 업로드 시간 (예: 18시 30분)'
);

-- 2. 가이드 멘트 풀 테이블 생성
CREATE TABLE guide_message_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT COMMENT '상위 가이드 FK',
    content TEXT NOT NULL COMMENT '실제 가이드 문구 내용',
);