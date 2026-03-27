package com.example.demo.controller;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import com.example.demo.dto.channel.PlatformKeywordResponseDto;
import com.example.demo.service.PlatformKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // 프론트 HTML 파일에서 호출 허용
public class PlatformKeywordController {

    private final PlatformKeywordService platformKeywordService;

    @GetMapping("/keywords")
    public ResponseEntity<PlatformKeywordResponseDto> getKeywords(
            @RequestParam Long brandId) {  // RequestBody → RequestParam으로 변경

        PlatformKeywordResponseDto response = platformKeywordService.getKeywords(brandId);
        return ResponseEntity.ok(response);
    }
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 검색어 노드를 띄우기 위한 호출

*/