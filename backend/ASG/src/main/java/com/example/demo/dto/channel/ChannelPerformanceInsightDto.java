package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelPerformanceInsightDto(
        String periodType,
        Integer baseYear,
        Integer baseMonth,
        Double weekendEffectScore,
        Double holidayEffectScore,
        Integer bestDayOfWeek,
        String bestHourRange
) {
}