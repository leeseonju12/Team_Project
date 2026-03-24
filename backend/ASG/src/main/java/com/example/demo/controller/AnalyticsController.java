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

    // 🌟 버튼 하나로 모든 피드 지표 동기화!
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
    
 // 🌟 프론트엔드에서 호출할 '게시물 목록 조회' API
    @GetMapping("/posts/facebook")
    public ResponseEntity<?> getFacebookPosts() {
        try {
            List<Map<String, String>> posts = analyticsService.getFacebookPostList();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("목록 조회 실패: " + e.getMessage());
        }
    }
}