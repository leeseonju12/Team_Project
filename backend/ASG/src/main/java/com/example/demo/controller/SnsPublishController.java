package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PublishRequest;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.FacebookApiService;
import com.example.demo.service.InstagramApiService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sns")
public class SnsPublishController {

    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v19.0/";
    
    private final InstagramApiService instagramApiService;
    private final FacebookApiService facebookApiService;
    private final CloudinaryService cloudinaryService;

    
    @PostMapping("/publish/instagram")
    public ResponseEntity<?> publishToInstagram(@RequestBody PublishRequest request) {
        try {
            // 서비스 호출해서 발행 완료 후 게시물 ID 받아오기
            String postId = instagramApiService.publishPost(request.getImageUrl(), request.getCaption());
            
            // 프론트엔드에 성공 메시지 던져주기
            return ResponseEntity.ok(Map.of("message", "성공적으로 게시되었습니다!", "postId", postId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
        	//String imageUrl = imgbbService.uploadImage(file);
        	String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/publish/facebook")
    public ResponseEntity<?> publishToFacebook(@RequestBody PublishRequest request) {
        try {
            String postId = facebookApiService.publishPost(request.getImageUrl(), request.getCaption());
            return ResponseEntity.ok(Map.of("message", "페이스북에 성공적으로 게시되었습니다!", "postId", postId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    
}