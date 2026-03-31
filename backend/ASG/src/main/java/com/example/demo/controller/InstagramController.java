package com.example.demo.controller;

import com.example.demo.service.InstagramApiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstagramController {

    private final InstagramApiService instagramApiService;

    public InstagramController(InstagramApiService instagramApiService) {
        this.instagramApiService = instagramApiService;
    }

    // 주소창에 ?token=파란색장기토큰 을 넣어서 테스트할 엔드포인트
    @GetMapping("/instagram/data")
    public String getInstagramData() {
        try {
            // 1. 인스타그램 비즈니스 ID 가져오기
            String igAccountId = instagramApiService.getInstagramAccountId();
            
            // 2. 해당 ID로 피드(게시물) 가져오기
            JsonNode feedData = instagramApiService.getInstagramFeed(igAccountId);

            // 3. 화면에 보기 좋게 출력
            return "<h1>인스타그램 연동 성공! 🎉</h1>" +
                   "<h3>내 인스타그램 계정 ID: " + igAccountId + "</h3>" +
                   "<hr>" +
                   "<h3>최신 게시물 데이터 (JSON):</h3>" +
                   "<pre>" + feedData.toPrettyString() + "</pre>";

        } catch (Exception e) {
            return "<h1>에러 발생 😢</h1><p>" + e.getMessage() + "</p>";
        }
    }
}