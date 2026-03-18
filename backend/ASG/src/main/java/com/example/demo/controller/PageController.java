package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.InstagramApiService;

@Controller
public class PageController {

	@Autowired
    private InstagramApiService instagramApiService;
	
	@GetMapping("/feedback")
	public String feedbackPage() {
		
		System.out.println("===PageController: 대시보드 접속 & 동기화 시작 ===");
        
        try {
            // 화면 띄우기 직전에 싹 긁어오기!
            int newComments = instagramApiService.syncAllInstagramComments();
            System.out.println("동기화 완료! 새로 추가된 댓글 수: " + newComments);
        } catch (Exception e) {
            System.err.println("동기화 중 에러 발생: " + e.getMessage());
        }	

		return "forward:/feedback.html";
	}
	
	

	@GetMapping("/channel-performance")
	public String channelPerformancePage() {
		return "forward:/channel-performance.html";
	}
}