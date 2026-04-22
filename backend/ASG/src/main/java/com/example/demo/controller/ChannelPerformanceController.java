package com.example.demo.controller;

import com.example.demo.service.ChannelPerformanceService;
import com.example.demo.service.myPage.MypageService;
import com.example.demo.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/channel-performance")
@RequiredArgsConstructor
public class ChannelPerformanceController {

    private final ChannelPerformanceService channelPerformanceService;
    private final MypageService mypageService;

    @GetMapping
    public ResponseEntity<?> getDashboard(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (principalDetails == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = principalDetails.getUser().getId();
        Long brandId = mypageService.getBrandId(userId);

        LocalDate endDate = (to == null) ? LocalDate.now() : to;
        LocalDate startDate = (from == null) ? endDate.minusDays(30) : from;

        // 이전 기간 계산 (현재 기간과 동일한 길이, 시작/종료일 포함)
        long periodDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevEndDate = startDate.minusDays(1);
        LocalDate prevStartDate = prevEndDate.minusDays(periodDays - 1);

        return ResponseEntity.ok(
                channelPerformanceService.getDashboard(
                        brandId,
                        startDate,
                        endDate,
                        prevStartDate,
                        prevEndDate
                )
        );
    }
}