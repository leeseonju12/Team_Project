package com.example.demo.calendar.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {
    PENDING("대기 중"),
    SCHEDULED("예약됨"),
    PUBLISHED("발행 완료");

    private final String description;
}