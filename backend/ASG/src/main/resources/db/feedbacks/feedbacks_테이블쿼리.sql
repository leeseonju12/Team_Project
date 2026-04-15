## feedbacks 테이블과 컬럼들

CREATE TABLE customer_feedback (
    feedback_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '피드백 ID',
    external_id VARCHAR(255) NOT NULL UNIQUE COMMENT '플랫폼별 댓글 고유 ID',
    author_name VARCHAR(255) COMMENT '작성자명',
    original_text TEXT COMMENT '원문',
    origin_url VARCHAR(255) COMMENT '원문 링크',
    platform VARCHAR(50) COMMENT '플랫폼 (향후 ENUM 검토 필요)',
    TYPE ENUM('COMMENT', 'REVIEW') COMMENT '피드백 타입 (리뷰/댓글)',
    STATUS ENUM('CHECKED', 'COMPLETED', 'SENDING', 'UNCHECKED', 'UNRESOLVED') COMMENT '전체 진행 상태',
    ai_status ENUM('DONE', 'IDLE') COMMENT 'AI 응답 상태',
    ai_reply TEXT COMMENT 'AI 응답 내용',
    sent_reply TEXT COMMENT '보낸 응답 내용',
    source_id BIGINT(20) COMMENT '플랫폼별 게시글 고유 ID',
    created_at DATETIME COMMENT '댓글 작성일',
    updated_at DATETIME COMMENT '피드백 업데이트일',
    PRIMARY KEY (feedback_id),
    INDEX idx_customer_feedback_created_at (created_at DESC)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='피드백 리스트';