package com.example.demo.controller;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import com.example.demo.repository.BrandRepository;
import com.example.demo.service.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class AiStrategyController {

    private final GeminiApiClient geminiClient;
    private final BrandRepository brandRepository; // Brand 조회용
    private static final Logger log = LoggerFactory.getLogger(AiStrategyController.class);
    
    //TODO - 로그인 한 사용자 정보로 바꿔야 함
    @PostMapping("/strategy")
    public ResponseEntity<Map<String, String>> strategy(
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "19") Long brandId) {  // 일단 강제 지정

        log.info("====> [AI전략] 분석 요청 수신 (brandId: {})", brandId);

        // Brand 정보 조회
        BrandSearchRequestDto brand = brandRepository.findById(brandId);
        // 수정
        String brandName    = brand != null ? brand.getBrandName()    : "알 수 없는 브랜드";
        String industryType = brand != null ? brand.getIndustryType() : "일반";
        
        log.info("====> [AI전략] 브랜드 정보 조회 완료 - 브랜드명: {}, 업종: {}", brandName, industryType);

        String prompt = body.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            log.warn("====> [AI전략] 프롬프트가 비어있음");
            return ResponseEntity.badRequest().body(Map.of("result", "[]"));
        }

        // 프롬프트 앞에 브랜드 정보 추가
        String enrichedPrompt = String.format(
            "분석 대상 브랜드: %s (업종: %s)\n\n%s",
            brandName, industryType, prompt
        );

        log.info("====> [AI전략] Gemini API 호출 시작 (프롬프트 길이: {}자)", enrichedPrompt.length());
        long start = System.currentTimeMillis();

        String result = geminiClient.requestToGemini(enrichedPrompt);

        long elapsed = System.currentTimeMillis() - start;
        log.info("====> [AI전략] Gemini 응답 완료 (소요시간: {}ms)", elapsed);

        return ResponseEntity.ok(Map.of("result", result));
    }
}