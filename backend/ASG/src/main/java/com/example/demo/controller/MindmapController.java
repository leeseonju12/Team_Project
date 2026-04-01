package com.example.demo.controller;

import com.example.demo.dto.channel.MindmapSearchResponseDto;
import com.example.demo.service.MindmapSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mindmap")
@RequiredArgsConstructor
public class MindmapController {

    private final MindmapSearchService mindmapService;
    
 // TODO - 로그인 된 회원의 브랜드 아이디로 바꿔야 함
    
    /**
     * 연관 키워드 조회
     * GET /api/mindmap?brandId=??
     * Redis 캐시 있으면 바로 반환, 없으면 SerpAPI 호출 후 캐시 저장
     */
    @GetMapping
    public MindmapSearchResponseDto getRelatedKeywords(
            @RequestParam(defaultValue = "19") Long brandId) {
        return mindmapService.getRelatedKeywords(brandId);
    }

    /**
     * 캐시 강제 갱신 (관리자용)
     * POST /api/mindmap/refresh?brandId=??
     * Redis 캐시 삭제 후 SerpAPI 재호출 (6개월마다 또는 수동 갱신 시 사용)
     */
    @PostMapping("/refresh")
    public MindmapSearchResponseDto refreshRelatedKeywords(
            @RequestParam(defaultValue = "19") Long brandId) {
        return mindmapService.refreshRelatedKeywords(brandId);
    }
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
CacheConfig 에서 정의한 시간에 맞게 초기화 중

*/
 