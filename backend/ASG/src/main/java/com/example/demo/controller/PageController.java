package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/feedbacks")
	public String feedbackPage() {

		return "forward:/feedback.html";
	}
	

	@GetMapping("/channel-performance")
	public String channelPerformancePage() {
	    // templates/channel-performance.html 을 찾으려면 확장자(.html) 없이 이름만 적습니다.
	    return "channel-performance"; 
	}
	
	@GetMapping("/search-test")
	public String searchTestPage() {
		return "forward:/imgSearchTest.html";
	}
	
	@GetMapping("/sns/pexels") 
    public String showPexelsPage() {
        return "pexels"; 
    }
	

	@GetMapping("/temp3")
	public String temp3Page(Model model) {
	    // 헤더에서 userInfo를 참조하므로, 에러 방지를 위해 가짜 데이터를 넣어줍니다.
	    Map<String, String> fakeUser = new HashMap<>();
	    fakeUser.put("name", "테스트");
	    fakeUser.put("email", "test@example.com");
	    
	    model.addAttribute("userInfo", fakeUser);
	    return "temp3";
	}
}