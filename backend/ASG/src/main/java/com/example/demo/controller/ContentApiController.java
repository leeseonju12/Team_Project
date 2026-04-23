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

import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam(value = "externalUrls", required = false) String externalUrls,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "pexelsUrl", required = false) String pexelsUrl) {
        
        try {
            System.out.println("[Operator: 서희] SNS Publishing via Cloudinary Direct - Platform: " + platform);
            
            /*
             * Rationale: 
             * 디버그 로그를 통해 프론트엔드가 'externalUrls' 키를 사용하여 클라우디너리 URL을
             * 전달하고 있음을 확인했습니다. RequestParam 매핑을 이에 맞춰 수정하여 데이터를 수신합니다.
             */
            String realImageUrl = externalUrls;

            /*
             * Rationale: 
             * externalUrls가 null이거나 빈 문자열인 경우에만 차선책으로 
             * 직접 파일 업로드 또는 Pexels URL 파싱 로직을 실행하도록 분기합니다.
             */
            if (!org.springframework.util.StringUtils.hasText(realImageUrl)) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    realImageUrl = cloudinaryService.uploadImage(imageFile);
                } else if (org.springframework.util.StringUtils.hasText(pexelsUrl)) {
                    realImageUrl = cloudinaryService.uploadImageFromUrl(pexelsUrl);
                }
            }

            /*
             * Rationale: 
             * 플랫폼별 API 호출 전 최종적으로 유효한 이미지 URL이 확보되었는지 검증합니다.
             * 여기서 통과하면 Facebook, Instagram API에서 발생하는 이미지 누락 예외를 방지할 수 있습니다.
             */
            if (!org.springframework.util.StringUtils.hasText(realImageUrl)) {
                throw new IllegalArgumentException("이미지 처리에 실패했습니다. 업로드 파라미터가 누락되었거나 필수 이미지가 없습니다.");
            }

            if ("instagram".equalsIgnoreCase(platform)) {
                instagramService.publishPost(realImageUrl, text);
                return Map.of("status", "success", "message", "Instagram 게시 완료!");
            } else if ("facebook".equalsIgnoreCase(platform)) {
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