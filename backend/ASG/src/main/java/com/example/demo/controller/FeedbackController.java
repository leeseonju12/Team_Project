package com.example.demo.controller; // 본인 패키지명으로 변경하세요

import com.example.demo.dto.FeedbackDto;

import com.example.demo.service.FeedbackService;
import com.example.demo.service.InstagramApiService;

import com.example.demo.domain.CustomerFeedback;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
	
	private final InstagramApiService instagramApiService;

    private final FeedbackService feedbackService;
    /*
	@GetMapping("/feedback")
    public String feedbackPage() {
		
        return "forward:/feedback.html"; 
    }
    */

    // GET http://localhost:8080/api/feedbacks 호출 시 작동
    @GetMapping
    public List<FeedbackDto> getFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @PutMapping("/{id}/ai-reply")
    public ResponseEntity<?> generateAiReply(@PathVariable Long id) {
    	try {
    	FeedbackDto response = feedbackService.generateAiReply(id);
        return ResponseEntity.ok(response);
    	} catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    //개별 답변 전송 API
    @PutMapping("/{id}/send")
    public ResponseEntity<?> sendReply(@PathVariable Long id) {
    	
    	try {
    		FeedbackDto response = feedbackService.sendReply(id);
            return ResponseEntity.ok(response);
        } 
    	catch (Exception e) {
            return ResponseEntity.internalServerError()
                                 .body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/instagram/sync-comments")
    @ResponseBody
    public ResponseEntity<?> syncAllComments() {
        try {
            // 서비스 계층의 전체 동기화 메서드 호출
            int newCount = instagramApiService.syncAllInstagramComments();
            
            return ResponseEntity.ok(Map.of(
                "message", "동기화 완료",
                "newCount", newCount // 새로 추가된 댓글 개수를 프론트에 알려줍니다.
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
    
    
    
}