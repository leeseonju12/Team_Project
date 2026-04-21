package com.example.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import com.example.demo.dto.ContentInitResponse;
import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.dto.SnsResult;
import com.example.demo.entity.GeneratedContent;
import com.example.demo.repository.GeneratedContentRepository;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ContentService;
import com.example.demo.service.FacebookApiService;
import com.example.demo.service.GeneratedContentService;
import com.example.demo.service.InstagramApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ContentApiController {

    private final ContentService contentService;
    private final InstagramApiService instagramService; 
    private final FacebookApiService facebookService;
    private final CloudinaryService cloudinaryService;
    private final GeneratedContentRepository contentRepository;
    private final GeneratedContentService generatedContentService;
    
    @GetMapping("/init")
    public ResponseEntity<ContentInitResponse> getInitialData(
            @RequestParam String industryCode, 
            @RequestParam Long userId) {
        return ResponseEntity.ok(contentService.getInitialData(industryCode, userId));
    }

    /* Rationale: 미배정 컨텐츠 목록 조회 */
    @GetMapping("/pending")
    public ResponseEntity<List<GeneratedContent>> getPendingPosts() {
        List<GeneratedContent> pendingContents = contentRepository.findByScheduledDateIsNullOrderByCreatedAtDesc();
        return ResponseEntity.ok(pendingContents);
    }

    /* Rationale: 캘린더 일정 데이터 조회 (FullCalendar 포맷) */
    @GetMapping("/events")
    public List<Map<String, Object>> getCalendarEvents() {
        return contentRepository.findByScheduledDateIsNotNull().stream().map(content -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", content.getId());
            event.put("title", content.getMenuName()); 
            event.put("start", content.getScheduledDate()); 
            event.put("color", "#3788d8");

            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("content", content.getContent());
            extendedProps.put("platform", content.getPlatform());
            extendedProps.put("hashtags", content.getHashtags()); 

            event.put("extendedProps", extendedProps);
            return event;
        }).collect(Collectors.toList());
    }

    /* Rationale: 일정 저장 및 변경 (드래그 앤 드롭) */
    @PostMapping("/schedule")
    public ResponseEntity<?> updateSchedule(@RequestBody ScheduleRequestDto request) {
        try {
            generatedContentService.updateContentSchedule(request.getContentId(), request.getScheduledDate());
            return ResponseEntity.ok(Map.of("success", true, "message", "일정이 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public Map<String, String> generateForApi(HttpSession session,
                                              @RequestBody ContentRequest request) {
        Long userId = (Long) session.getAttribute("userId");
        request.setUserId(userId);
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
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "pexelsUrl", required = false) String pexelsUrl) {
        try {
            System.out.println("[Operator: 서희] SNS Publishing via Cloudinary Direct - Platform: " + platform);
            String realImageUrl = null;

            /* * Rationale: 
             * 다운로드 래퍼 클래스를 제거하고, CloudinaryService의 오버로딩된 메서드를 직접 호출하여
             * 중간 병목(Middle Bottleneck)을 제거합니다.
             */
            if (imageFile != null && !imageFile.isEmpty()) {
                realImageUrl = cloudinaryService.uploadImage(imageFile);
            } else if (StringUtils.hasText(pexelsUrl)) {
                // Cloudinary로 직접 URL 전달
                realImageUrl = cloudinaryService.uploadImageFromUrl(pexelsUrl);
            }

            if ("instagram".equalsIgnoreCase(platform)) {
                if (realImageUrl == null)
                    throw new RuntimeException("인스타그램은 이미지가 필수입니다.");
                instagramService.publishPost(realImageUrl, text);
                return Map.of("status", "success", "message", "Instagram 게시 완료!");
            } else if ("facebook".equalsIgnoreCase(platform)) {
                if (realImageUrl == null)
                    throw new RuntimeException("페이스북 /photos API는 이미지가 필수입니다.");
                facebookService.publishPost(realImageUrl, text);
                return Map.of("status", "success", "message", "Facebook 게시 완료!");
            }

            return Map.of("status", "fail", "message", "지원하지 않는 플랫폼입니다.");

        } catch (Exception e) {
            System.err.println("[" + platform + "] 발행 실패: " + e.getMessage());
            return Map.of("status", "fail", "message", e.getMessage());
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImages(
            @RequestParam("uploadFiles") List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Save logic here
            }
        }
        return ResponseEntity.ok("Upload successful");
    }
    
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();
        List<String> uploadedUrls = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file);
                    uploadedUrls.add(imageUrl);
                }
            }
            response.put("status", "success");
            response.put("urls", uploadedUrls);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /* Rationale: 외부 이미지 URL 다운로드 후 byte[] 반환 */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    /* Rationale: CloudinaryService의 기존 시그니처 호환성을 맞추기 위한 Custom MultipartFile 구현체 */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;

        public ByteArrayMultipartFile(byte[] content, String name) {
            this.content = content;
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return "image/jpeg"; }
        @Override public boolean isEmpty() { return content == null || content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() throws IOException { return content; }
        @Override public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}