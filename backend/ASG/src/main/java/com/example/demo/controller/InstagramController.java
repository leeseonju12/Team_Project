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

    @GetMapping("/instagram/post")
    public String postToInstagram() {
        try {
            // 1. 내 인스타그램 ID 가져오기
            String igAccountId = instagramApiService.getInstagramAccountId();
            
        	
            // 2. 테스트용 공개 이미지 URL (인터넷에서 접근 가능한 아무 이미지나 가능)
            String testImageUrl = "https://images.unsplash.com/photo-1505628346881-b72b27e84530?w=500&auto=format&fit=crop";
            
            // 3. 테스트용 내용
            String caption = "나는 행복합니다~ 🦅🧡 API 자동 게시 테스트 중입니다!\n\n#개발자 #테스트";

            // 4. 서비스 호출하여 글 올리기!
            String publishedId = instagramApiService.publishInstagramPost(igAccountId, testImageUrl, caption);

            return "<h1>게시 성공! 🎉</h1>" +
                   "<p>인스타그램 피드를 확인해 보세요!</p>" +
                   "<p>발행된 게시물 ID: " + publishedId + "</p>";

        } catch (Exception e) {
            return "<h1>업로드 실패 😢</h1><p>" + e.getMessage() + "</p>";
        }
    }
}