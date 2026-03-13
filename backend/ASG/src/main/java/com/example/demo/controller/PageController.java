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
		return "forward:/channel-performance.html";
	}
}