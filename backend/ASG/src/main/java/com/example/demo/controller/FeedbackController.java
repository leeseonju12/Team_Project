package com.example.demo.controller; // 본인 패키지명으로 변경하세요

import com.example.demo.dto.FeedbackDto;
import com.example.demo.service.FeedbackService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

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
    public FeedbackDto generateAiReply(@PathVariable Long id) {
        return feedbackService.generateAiReply(id);
    }
    
    //개별 답변 전송 API
    @PutMapping("/{id}/send")
    public ResponseEntity<?> sendReply(@PathVariable Long id, @RequestParam("accessToken") String accessToken) {
        
    	System.out.println("토큰 정상적으로 받음");
    	
    	try {
    		FeedbackDto response = feedbackService.sendReply(id, accessToken);
            return ResponseEntity.ok(response);
        } 
    	catch (Exception e) {
            return ResponseEntity.internalServerError()
                                 .body(Map.of("message", e.getMessage()));
        }
    }
    
    
}