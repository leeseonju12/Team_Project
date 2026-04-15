package com.example.demo.controller.customerCenter;

import com.example.demo.entity.customerCenter.Reply;
import com.example.demo.entity.myPage.Inquiry;
import com.example.demo.repository.customerCenter.ReplyRepository;
import com.example.demo.repository.myPage.InquiryRepository;
import com.example.demo.service.customerCenter.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyRepository replyRepository;
    private final InquiryRepository inquiryRepository;
    private final EmailService emailService;

    // 답변 목록 조회
    @GetMapping("/api/admin/replies/{inquiryId}")
    public List<Reply> getReplies(@PathVariable Long inquiryId) {
        return replyRepository.findByInquiryIdOrderByIdAsc(inquiryId);
    }

    // 답변 등록 + 이메일 발송
    @PostMapping("/api/admin/replies/{inquiryId}")
    public Reply createReply(@PathVariable Long inquiryId, @RequestBody Map<String, String> body) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank()) throw new RuntimeException("답변 내용을 입력하세요.");

        Reply reply = new Reply();
        reply.setInquiryId(inquiryId);
        reply.setContent(content);

        Reply savedReply = replyRepository.save(reply);
        
     // 답변 등록 시 상태 자동 처리완료 변경
        inquiry.setStatus("처리완료");
        inquiryRepository.save(inquiry);

        /* 답변 등록 후 사용자 이메일 발송, 비활성
        emailService.sendReplyToUser(
                inquiry.getEmail(),
                inquiry.getTitle(),
                content
        );*/
        
     // 이메일 발송 (더미 계정 사용 중 — 실패해도 답변 등록은 정상 처리)
        try {
            emailService.sendReplyToUser(
                    inquiry.getEmail(),
                    inquiry.getTitle(),
                    content
            );
        } catch (Exception e) {
            System.out.println("[이메일 발송 스킵] " + e.getMessage());
        }

        return savedReply;
    }

    // 답변 수정
    @PutMapping("/api/admin/replies/{replyId}/edit")
    public Reply updateReply(@PathVariable Long replyId, @RequestBody Map<String, String> body) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));

        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank()) throw new RuntimeException("답변 내용을 입력하세요.");

        reply.setContent(content);
        return replyRepository.save(reply);
    }

    // 답변 삭제
 // 수정 후
    @DeleteMapping("/api/admin/replies/{replyId}")
    public Map<String, String> deleteReply(@PathVariable Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));

        Long inquiryId = reply.getInquiryId();
        replyRepository.delete(reply);

        // 남은 답변이 0개면 문의 상태를 미처리로 복구
        List<Reply> remaining = replyRepository.findByInquiryIdOrderByIdAsc(inquiryId);
        if (remaining.isEmpty()) {
            Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);
            if (inquiry != null) {
                inquiry.setStatus("미처리");
                inquiryRepository.save(inquiry);
            }
        }

        return Map.of("result", "ok");
    }
}
