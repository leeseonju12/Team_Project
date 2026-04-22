package com.example.demo.controller;

import com.example.demo.dto.channel.MindmapSearchResponseDto;
import com.example.demo.service.MindmapSearchService;
import com.example.demo.service.myPage.MypageService;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import com.example.demo.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mindmap")
@RequiredArgsConstructor
public class MindmapController {

    private final MindmapSearchService mindmapService;
    private final MypageService mypageService;

    /**
     * 연관 키워드 조회
     * GET /api/mindmap
     * brandId는 세션에서 자동 조회
     */
    @GetMapping
     public ResponseEntity<?> getRelatedKeywords(@AuthenticationPrincipal PrincipalDetails principalDetails) {
         if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
         Long userId = principalDetails.getUser().getId();
        Long brandId = mypageService.getBrandId(userId);
        return ResponseEntity.ok(mindmapService.getRelatedKeywords(brandId));
    }

    /**
     * 캐시 강제 갱신 (관리자용)
     * POST /api/mindmap/refresh
     */
    @PostMapping("/refresh")
     public ResponseEntity<?> refreshRelatedKeywords(@AuthenticationPrincipal PrincipalDetails principalDetails) {
         if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
         Long userId = principalDetails.getUser().getId();
        Long brandId = mypageService.getBrandId(userId);
        return ResponseEntity.ok(mindmapService.refreshRelatedKeywords(brandId));
    }
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
CacheConfig 에서 정의한 시간에 맞게 초기화 중

*/
 