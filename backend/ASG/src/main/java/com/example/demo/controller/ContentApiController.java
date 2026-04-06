package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ContentService;
import com.example.demo.service.FacebookApiService;
// 💡 기존에 잘 만들어두신 InstagramApiService를 임포트합니다.
import com.example.demo.service.InstagramApiService; 

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentApiController {

    private final ContentService contentService;
    private final InstagramApiService instagramService; 
    private final FacebookApiService facebookService;
    private final CloudinaryService cloudinaryService;

    // 1. AI 생성 로직 (수정 없음)
    @PostMapping("/generate")
    public Map<String, String> generateForApi(@RequestBody ContentRequest request) {
        List<SnsResult> results = contentService.generateAllSnsContent(request);
        return results.stream().collect(Collectors.toMap(
            res -> res.getPlatform().toLowerCase(), 
            res -> res.getContent()                 
        ));
    }
    @PostMapping("/publish")
    public Map<String, Object> publishToSns(
            @RequestParam("platform") String platform,
            @RequestParam("text") String text,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        System.out.println("프론트에서 온 [" + platform + "] 발행 요청! 텍스트: " + text);

        try {
            String realImageUrl = null;

            // 1. 공통: 이미지가 넘어왔다면 Cloudinary에 업로드하고 HTTPS URL을 받아옵니다.
            if (imageFile != null && !imageFile.isEmpty()) {
                realImageUrl = cloudinaryService.uploadImage(imageFile);
                System.out.println("Cloudinary 업로드 성공! 실제 URL: " + realImageUrl);
            } 

            // 2. 플랫폼별 발행 로직 분기
            if ("instagram".equalsIgnoreCase(platform)) {
                if (realImageUrl == null) {
                    throw new RuntimeException("인스타그램 게시는 이미지가 필수입니다.");
                }
                String publishedMediaId = instagramService.publishPost(realImageUrl, text);
                return Map.of("status", "success", "message", "Instagram 게시 완료!");

            } else if ("facebook".equalsIgnoreCase(platform)) { // 💡 페이스북 분기 추가
                
                // 페이스북은 텍스트만으로도 게시가 가능하지만, 
                // 현재 FacebookApiService.publishPost 는 /photos 엔드포인트를 쓰므로 이미지가 필수입니다.
                if (realImageUrl == null) {
                    throw new RuntimeException("페이스북 /photos API는 이미지가 필수입니다.");
                    // 만약 이미지 없이 글만 올리고 싶다면 FacebookApiService에 /feed 엔드포인트를 쓰는
                    // publishTextPost 메서드를 따로 만드셔야 합니다.
                }
                
                String publishedPostId = facebookService.publishPost(realImageUrl, text);
                System.out.println("페이스북 실제 게시 완료! 포스트 ID: " + publishedPostId);
                return Map.of("status", "success", "message", "Facebook 게시 완료!");

            }

            return Map.of("status", "fail", "message", "지원하지 않는 플랫폼입니다.");

        } catch (Exception e) {
            System.err.println("[" + platform + "] 발행 실패: " + e.getMessage());
            return Map.of("status", "fail", "message", e.getMessage());
        }
    }
}