package com.example.demo.controller;

import com.example.demo.dto.channel.NaverSearchResponseDto;
import com.example.demo.service.NaverSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/naver-search")
@RequiredArgsConstructor
public class NaverSearchController {

    private final NaverSearchService naverSearchService;

    /**
     * GET /api/naver-search?brandId=1&period=month&from=2024-12-01&to=2024-12-31
     * period: week | month | year  (기본값 month)
     */
    @GetMapping
    public NaverSearchResponseDto getDashboard(
            @RequestParam(defaultValue = "1")    Long brandId,
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate   = (to   == null) ? LocalDate.of(2024, 12, 31) : to;
        LocalDate startDate = (from == null) ? endDate.minusDays(30)      : from;

        return naverSearchService.getDashboard(brandId, startDate, endDate, period);
    }
}