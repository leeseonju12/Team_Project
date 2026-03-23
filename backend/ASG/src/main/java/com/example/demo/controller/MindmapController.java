package com.example.demo.controller;

import com.example.demo.dto.channel.MindmapResponseDto;
import com.example.demo.service.MindmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mindmap")
@RequiredArgsConstructor
public class MindmapController {

    private final MindmapService mindmapService;

    /**
     * 연관 키워드 조회
     * GET /api/mindmap?brandId=18
     * Redis 캐시 있으면 바로 반환, 없으면 SerpAPI 호출 후 캐시 저장
     */
    @GetMapping
    public MindmapResponseDto getRelatedKeywords(
            @RequestParam(defaultValue = "18") Long brandId) {
        return mindmapService.getRelatedKeywords(brandId);
    }

    /**
     * 캐시 강제 갱신 (관리자용)
     * POST /api/mindmap/refresh?brandId=18
     * Redis 캐시 삭제 후 SerpAPI 재호출 (6개월마다 또는 수동 갱신 시 사용)
     */
    @PostMapping("/refresh")
    public MindmapResponseDto refreshRelatedKeywords(
            @RequestParam(defaultValue = "18") Long brandId) {
        return mindmapService.refreshRelatedKeywords(brandId);
    }
}