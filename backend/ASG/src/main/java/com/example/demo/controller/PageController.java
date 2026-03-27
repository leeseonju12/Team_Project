package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/feedback")
	public String feedbackPage() {

		return "forward:/feedback.html";
	}
	

	@GetMapping("/channel-performance")
	public String channelPerformancePage() {
	    // templates/channel-performance.html 을 찾으려면 확장자(.html) 없이 이름만 적습니다.
	    return "channel-performance"; 
	}
}