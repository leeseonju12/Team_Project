package com.example.demo.domain.calendar.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentStatus { 
    
    PENDING("대기 중"),
    SCHEDULED("예약됨"),
    PUBLISHED("발행 완료");

    private final String description;

    // 만약 "PENDING"이라는 문자열을 통해 Enum을 찾고 싶을 때 유용한 로직을 추가할 수 있습니다.
    public static ContentStatus fromString(String text) {
        for (ContentStatus status : ContentStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("일치하는 상태값이 없습니다: " + text);
    }
}