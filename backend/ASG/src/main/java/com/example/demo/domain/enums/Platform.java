package com.example.demo.domain.enums;

public enum Platform {
	NAVER("네이버"),
    KAKAO("카카오"),
    GOOGLE("구글"),
    INSTAGRAM("인스타그램"),
    FACEBOOK("페이스북");
	
	private final String label;

    Platform(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public FeedbackType getAutoType() {
        if (this == INSTAGRAM || this == FACEBOOK) {
            return FeedbackType.COMMENT; // 인스타, 페북은 '댓글'
        }
        return FeedbackType.REVIEW;      // 나머지는 '리뷰'
    }

}
