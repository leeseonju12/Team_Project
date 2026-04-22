package com.example.demo.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum IndustryType {
    CAFE("카페 / 베이커리"),
    FOOD("음식점 / 식당"),
    BEAUTY("미용 / 뷰티"),
    FASHION("패션 / 의류"),
    STAY("숙박 / 펜션"),
    SPORTS("피트니스 / 스포츠"),
    EDU("교육 / 학원"),
    MED("의료 / 병원"),
    RETAIL("소매 / 쇼핑"),
    ETC("기타");

    private final String description;

    // ✅ 추가: 한글 문자열을 받아 알맞은 Enum 객체로 변환
    public static IndustryType fromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return ETC; // 기본값 혹은 null 반환 정책 설정
        }
        return Stream.of(IndustryType.values())
                .filter(type -> type.getDescription().equals(description))
                .findFirst()
                .orElse(ETC); // 매칭되는 게 없으면 '기타'로 처리
    }
}