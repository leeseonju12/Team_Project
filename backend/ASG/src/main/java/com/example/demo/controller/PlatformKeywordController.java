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

    @PostMapping("/keywords")
    public ResponseEntity<PlatformKeywordResponseDto> getKeywords(
            @RequestBody BrandSearchRequestDto request) {

        PlatformKeywordResponseDto response = platformKeywordService.getKeywords(request);
        return ResponseEntity.ok(response);
    }
}