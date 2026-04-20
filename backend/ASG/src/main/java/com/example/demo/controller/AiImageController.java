package com.example.demo.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.user.entity.User;
import com.example.demo.dto.AiImageRequestDto;
import com.example.demo.dto.AiImageResponseDto;
import com.example.demo.service.AiImageService;
import com.example.demo.service.auth.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiImageController {
    private final AiImageService aiImageService;
    private final UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<AiImageResponseDto> generateAiImage(
            @RequestBody AiImageRequestDto request,
            Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // ✅ OAuth2에서 email 추출
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        String email = (String) oauthToken.getPrincipal().getAttributes().get("email");

        // ✅ findByEmail은 못 찾으면 IllegalArgumentException 던짐 (UserService 기존 로직)
        User user = userService.findByEmail(email);

        String category = user.getBusinessCategory();
        if (category == null || category.isBlank()) {
            throw new IllegalStateException("업종 정보가 설정되지 않았습니다. 프로필을 먼저 완성해주세요.");
        }

        log.info("AI 이미지 생성 요청 - email={}, category={}", email, category);

        AiImageResponseDto response = aiImageService.generateImage(
            request.getKeyword(),
            category,
            request.getDescription()
        );

        return ResponseEntity.ok(response);
    }
}