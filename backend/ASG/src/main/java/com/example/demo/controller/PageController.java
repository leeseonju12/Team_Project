package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.myPage.MypageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MypageService mypageService;

    /** 세션에서 userId 추출 */
    private Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

	@GetMapping("/feedbacks")
	public String feedbackPage(Model model) {
		
	    Map<String, String> fakeUser = new HashMap<>();
	    fakeUser.put("name", "테스트");
	    fakeUser.put("email", "test@example.com");

	    model.addAttribute("userInfo", fakeUser);
		return "feedback";
	}
	

	@GetMapping("/channel-performance")
	public String channelPerformancePage(HttpSession session, Model model) {
	    Long userId = getSessionUserId(session);
	    if (userId == null) return "redirect:/login_test";

	    Long brandId = mypageService.getBrandId(userId);
	    model.addAttribute("brandId", brandId);
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