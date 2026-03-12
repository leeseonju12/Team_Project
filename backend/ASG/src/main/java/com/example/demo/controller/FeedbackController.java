package com.example.demo.controller; // 본인 패키지명으로 변경하세요

import com.example.demo.dto.FeedbackResponseDto;
import com.example.demo.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // GET http://localhost:8080/api/feedbacks 호출 시 작동
    @GetMapping
    public List<FeedbackResponseDto> getFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }
    
    @PutMapping("/{id}/ai-reply")
    public FeedbackResponseDto generateAiReply(@PathVariable Long id) {
        return feedbackService.generateAiReply(id);
    }
}