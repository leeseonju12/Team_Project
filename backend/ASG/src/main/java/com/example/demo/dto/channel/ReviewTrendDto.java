package com.example.demo.dto.channel;

import lombok.Builder;
import java.util.List;

@Builder
public record ReviewTrendDto(
        String period,           // week / month / year
        List<String> labels,     // 날짜 레이블
        List<Integer> google,
        List<Integer> naver,
        List<Integer> kakao,
        List<Integer> total
) {
}