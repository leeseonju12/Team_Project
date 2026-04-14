package com.sosial.damoa.controller;

import com.sosial.damoa.entity.Inquiry;
import com.sosial.damoa.entity.Reply;
import com.sosial.damoa.repository.InquiryRepository;
import com.sosial.damoa.repository.ReplyRepository;
import com.sosial.damoa.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ReplyController {

    private final ReplyRepository replyRepository;
    private final InquiryRepository inquiryRepository;
    private final EmailService emailService;

    @GetMapping("/api/admin/replies/{inquiryId}")
    public List<Reply> getReplies(@PathVariable Long inquiryId) {
        return replyRepository.findByInquiryIdOrderByIdAsc(inquiryId);
    }

    @PostMapping("/api/admin/replies/{inquiryId}")
    public Reply createReply(@PathVariable Long inquiryId, @RequestBody Map<String, String> body) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank()) {
            throw new RuntimeException("답변 내용을 입력하세요.");
        }

        Reply reply = new Reply();
        reply.setInquiryId(inquiryId);
        reply.setContent(content);

        Reply savedReply = replyRepository.save(reply);

        // 답변 등록 후 사용자 이메일 발송
        emailService.sendReplyToUser(
                inquiry.getEmail(),
                inquiry.getTitle(),
                content
        );

        return savedReply;
    }

    @PutMapping("/api/admin/replies/{replyId}/edit")
    public Reply updateReply(@PathVariable Long replyId, @RequestBody Map<String, String> body) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));

        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank()) {
            throw new RuntimeException("답변 내용을 입력하세요.");
        }

        reply.setContent(content);
        return replyRepository.save(reply);
    }

    @DeleteMapping("/api/admin/replies/{replyId}")
    public Map<String, String> deleteReply(@PathVariable Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("답변 없음"));

        replyRepository.delete(reply);
        return Map.of("result", "ok");
    }
}