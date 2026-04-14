package com.sosial.damoa.controller;

import com.sosial.damoa.entity.Inquiry;
import com.sosial.damoa.repository.InquiryRepository;
import com.sosial.damoa.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final ReplyRepository replyRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 사용자 문의 등록
     * POST /api/public/inquiries
     */
    @PostMapping(value = "/api/public/inquiries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> create(
            @RequestParam("type") String type,
            @RequestParam("email") String email,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) throws IOException {

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> originalNames = new ArrayList<>();
        List<String> savedNames = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String originalName = file.getOriginalFilename();
                String savedName = UUID.randomUUID() + "_" + originalName;

                File dest = new File(dir, savedName);
                file.transferTo(dest);

                originalNames.add(originalName);
                savedNames.add(savedName);
            }
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setType(type);
        inquiry.setEmail(email);
        inquiry.setTitle(title);
        inquiry.setBody(body);
        inquiry.setStatus("미처리");
        inquiry.setAttachmentNames(String.join(",", originalNames));
        inquiry.setAttachmentSavedNames(String.join(",", savedNames));

        Inquiry saved = inquiryRepository.save(inquiry);

        return Map.of(
                "result", "ok",
                "id", saved.getId()
        );
    }

    /**
     * 사용자 본인 문의 조회
     * GET /api/inquiry?email=test@test.com
     *
     * 현재는 email 기준 조회 방식
     * 나중에 로그인 붙이면 email 파라미터 없이 로그인 사용자 기준으로 바꾸는 걸 추천
     */
    @GetMapping("/api/inquiry")
    public List<Map<String, Object>> getMyInquiries(@RequestParam("email") String email) {
        List<Inquiry> inquiries = inquiryRepository.findByEmailOrderByIdDesc(email);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Inquiry inquiry : inquiries) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", inquiry.getId());
            item.put("type", inquiry.getType());
            item.put("email", inquiry.getEmail());
            item.put("title", inquiry.getTitle());
            item.put("body", inquiry.getBody());
            item.put("status", inquiry.getStatus());

            item.put("attachmentNames", parseAttachmentNames(inquiry.getAttachmentNames()));

            result.add(item);
        }

        return result;
    }

    /**
     * 관리자 전체 문의 조회
     * GET /api/admin/inquiries
     */
    @GetMapping("/api/admin/inquiries")
    public List<Inquiry> getAll() {
        return inquiryRepository.findAllByOrderByIdDesc();
    }

    /**
     * 관리자 상태 변경
     * PATCH /api/admin/inquiries/{id}/status
     */
    @PatchMapping("/api/admin/inquiries/{id}/status")
    public Inquiry updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        inquiry.setStatus(body.get("status"));
        return inquiryRepository.save(inquiry);
    }

    /**
     * 관리자 문의 삭제
     * DELETE /api/admin/inquiries/{id}
     */
    @DeleteMapping("/api/admin/inquiries/{id}")
    @Transactional
    public Map<String, String> deleteInquiry(@PathVariable Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        if (inquiry.getAttachmentSavedNames() != null && !inquiry.getAttachmentSavedNames().isBlank()) {
            String[] savedNames = inquiry.getAttachmentSavedNames().split(",");
            for (String savedName : savedNames) {
                File file = new File(uploadDir, savedName.trim());
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        replyRepository.deleteByInquiryId(id);
        inquiryRepository.deleteById(id);

        return Map.of("result", "ok");
    }

    /**
     * 관리자 첨부파일 다운로드
     * GET /api/admin/inquiries/{id}/attachments/{index}
     */
    @GetMapping("/api/admin/inquiries/{id}/attachments/{index}")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable Long id,
            @PathVariable int index
    ) throws IOException {

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        if (inquiry.getAttachmentNames() == null || inquiry.getAttachmentSavedNames() == null) {
            throw new RuntimeException("첨부파일 없음");
        }

        String[] originalNames = inquiry.getAttachmentNames().split(",");
        String[] savedNames = inquiry.getAttachmentSavedNames().split(",");

        if (index < 0 || index >= savedNames.length || index >= originalNames.length) {
            throw new RuntimeException("첨부파일 인덱스 오류");
        }

        String originalName = originalNames[index].trim();
        String savedName = savedNames[index].trim();

        File file = new File(uploadDir, savedName);
        if (!file.exists()) {
            throw new RuntimeException("파일이 존재하지 않음");
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(encodedName, StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private List<String> parseAttachmentNames(String attachmentNames) {
        if (attachmentNames == null || attachmentNames.isBlank()) {
            return Collections.emptyList();
        }

        String[] split = attachmentNames.split(",");
        List<String> result = new ArrayList<>();

        for (String name : split) {
            if (name != null && !name.trim().isEmpty()) {
                result.add(name.trim());
            }
        }

        return result;
    }
}