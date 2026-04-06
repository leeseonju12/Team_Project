package com.example.demo.controller;

import com.example.demo.dto.channel.ChannelPerformanceResponseDto;
import com.example.demo.service.ChannelPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/channel-performance")
@RequiredArgsConstructor
public class ChannelPerformanceController {

    private final ChannelPerformanceService channelPerformanceService;

    @GetMapping
    public ChannelPerformanceResponseDto getDashboard(
            @RequestParam(defaultValue = "10") Long brandId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate = (to == null) ? LocalDate.now() : to;
        LocalDate startDate = (from == null) ? endDate.minusDays(30) : from;

        return channelPerformanceService.getDashboard(brandId, startDate, endDate);
    }
}