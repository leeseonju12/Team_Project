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
     */
    @GetMapping
    
    //TODO - 로그인 된 회원의 브랜드아이디로 변경해야함 
    public NaverSearchResponseDto getDashboard(
            @RequestParam(defaultValue = "10")    Long brandId,
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate   = (to   == null) ? LocalDate.now()       : to;
        LocalDate startDate = (from == null) ? endDate.minusDays(30) : from;
        return naverSearchService.getDashboard(brandId, startDate, endDate, period);
    }
}

/*
검색 수 = brand_name 이 검색된 횟수
주요 검색 사용자 = brand_name 을 검색한 연령대/성별
업종 인기 검색 키워드 = 각 업종별 인기 검색어 (다소 하드코딩)
상세 분석 = brand_name 기준

검색된 기록이 없다면 데이터가 없다는 문구가 나오도록 처리했고
유용한 데이터가 나타나게 하려면 실제 영업중인 업장 이름을 넣어야 합니다
 */