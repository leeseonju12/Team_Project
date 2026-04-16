package com.example.demo.controller.customerCenter;

import com.example.demo.entity.customerCenter.Notice;
import com.example.demo.repository.customerCenter.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;

    // 공개 조회 (고객센터 화면)
    @GetMapping("/api/public/notices")
    public List<Notice> getPublicNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    // 관리자 조회
    @GetMapping("/api/admin/notices")
    public List<Notice> getAdminNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    // 관리자 등록
    @PostMapping("/api/admin/notices")
    public Notice createNotice(@RequestBody Map<String, String> body) {
        String title    = body.getOrDefault("title", "").trim();
        String content  = body.getOrDefault("content", "").trim();
        String category = body.getOrDefault("category", "공지").trim();

        if (title.isBlank())   throw new RuntimeException("공지 제목을 입력하세요");
        if (content.isBlank()) throw new RuntimeException("공지 내용을 입력하세요");

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCategory(category.isBlank() ? "공지" : category);

        return noticeRepository.save(notice);
    }

    // 관리자 수정
    @PutMapping("/api/admin/notices/{id}")
    public Notice updateNotice(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지 없음"));

        String title    = body.getOrDefault("title", "").trim();
        String content  = body.getOrDefault("content", "").trim();
        String category = body.getOrDefault("category", "공지").trim();

        if (title.isBlank())   throw new RuntimeException("공지 제목을 입력하세요");
        if (content.isBlank()) throw new RuntimeException("공지 내용을 입력하세요");

        notice.setTitle(title);
        notice.setContent(content);
        notice.setCategory(category.isBlank() ? "공지" : category);

        return noticeRepository.save(notice);
    }

    // 관리자 삭제
    @DeleteMapping("/api/admin/notices/{id}")
    public Map<String, String> deleteNotice(@PathVariable Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지 없음"));

        noticeRepository.delete(notice);
        return Map.of("result", "ok");
    }
}
