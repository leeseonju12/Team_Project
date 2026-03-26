package com.example.demo.controller;

import com.example.demo.service.SocialAnalyticsService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final SocialAnalyticsService analyticsService;

    // 페이스북 지표 동기화
    @GetMapping("/sync/facebook/all")
    public ResponseEntity<String> syncAllFacebookMetrics() {
        Long testUserId = 1L; // 임시 유저 ID

        try {
            analyticsService.syncAllFacebookPosts(testUserId);
            return ResponseEntity.ok("페이스북 전체 피드 지표 동기화 대성공! DB를 확인해보세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("동기화 실패: " + e.getMessage());
        }
    }
    
    // 인스타그램 전체 피드 지표 동기화
    @GetMapping("/sync/instagram/all")
    public ResponseEntity<String> syncAllInstagramMetrics() {
        Long testUserId = 1L;

        try {
            analyticsService.syncAllInstagramPosts(testUserId);
            return ResponseEntity.ok("인스타그램 전체 피드 지표 동기화 성공");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("인스타그램 동기화 실패: " + e.getMessage());
        }
    }
}