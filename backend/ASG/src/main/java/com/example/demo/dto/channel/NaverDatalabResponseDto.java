package com.example.demo.dto.channel;

import java.util.List;

public record NaverDatalabResponseDto(
    String startDate,
    String endDate,
    String timeUnit,
    List<Result> results
) {
    public record Result(
        String title,
        List<String> keywords,
        List<DataPoint> data
    ) {}

    public record DataPoint(
        String period,  // "2024-01-01"
        double ratio    // 검색량 지수 (0~100)
    ) {}
}