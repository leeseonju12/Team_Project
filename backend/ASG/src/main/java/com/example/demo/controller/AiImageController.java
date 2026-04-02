package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.AiImageRequestDto;
import com.example.demo.dto.AiImageResponseDto;
import com.example.demo.service.AiImageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiImageController {
    private final AiImageService aiImageService;

    @PostMapping("/generate")
    public ResponseEntity<AiImageResponseDto> generateAiImage(
            @RequestBody AiImageRequestDto request) {

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리는 필수 입력 값입니다.");
        }

        AiImageResponseDto response = aiImageService.generateImage(
            request.getKeyword(), 
            request.getCategory()
        );
        return ResponseEntity.ok(response);
    }
}