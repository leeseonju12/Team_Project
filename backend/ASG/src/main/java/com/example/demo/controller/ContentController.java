package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.service.ContentService;
import com.example.demo.service.InstagramApiService; // 추가됨
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService geminiService;
    private final InstagramApiService instagramApiService; // 🌟 롬복이 자동 주입해 줌

    @GetMapping("/generate")
    public String showGeneratePage() {
        return "content-generate";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute ContentRequest request, Model model) {

        List<SnsResult> results = geminiService.generateAllSnsContent(request);
        
        model.addAttribute("results", results);
        model.addAttribute("req", request); // 입력했던 내용 유지용
        
        return "content-generate";
    }

    // 추가된 API 메서드 (게시하기 버튼에서 호출
    
    @PostMapping("/publish/instagram")
    @ResponseBody // 🌟 매우 중요: HTML 페이지 이동이 아니라 JSON 데이터를 반환하게 해줌!
    public ResponseEntity<?> publishToInstagram(@RequestBody Map<String, String> requestData) {
        try {
            // 프론트에서 보내준 텍스트 내용, 임시 이미지 URL, 토큰
            String caption = requestData.get("caption");
            String imageUrl = requestData.get("imageUrl");
            /*
            String accessToken = requestData.get("accessToken");

            if (accessToken == null || accessToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "액세스 토큰이 없습니다."));
            }*/

            // 1. 인스타그램 계정 ID 조회
            String igAccountId = instagramApiService.getInstagramAccountId();
            
            // 2. 피드 발행 (한글 깨짐 해결된 버전 호출)
            String publishedId = instagramApiService.publishInstagramPost(igAccountId, imageUrl, caption);

            // 성공 응답 (JSON)
            Map<String, String> result = new HashMap<>();
            result.put("message", "success");
            result.put("publishedId", publishedId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
  
    
    @GetMapping("/instagram/sync-comments")
    @ResponseBody
    public ResponseEntity<?> syncInstagramComments() {
        try {
            int newCommentCount = instagramApiService.syncAllInstagramComments();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "success");
            result.put("newCommentCount", newCommentCount);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
    
}