package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.domain.user.entity.User;
import com.example.demo.service.ContentService;
import com.example.demo.service.auth.UserService;

import com.example.demo.service.myPage.MypageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {
	
	private final UserService userService;
    private final MypageService mypageService;
	
    private Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
    
    @GetMapping("/feedbacks")
    public String feedbackPage(Model model, HttpSession session) {
        // 세션에서 userId 추출
        Long userId = getSessionUserId(session);


        // 세션 정보가 없는 경우 로그인 페이지로 리다이렉트
        if (userId == null) {
            return "redirect:/login";
        }

        // 실제 DB에서 유저 정보 조회
        User user = userService.findById(userId);
        model.addAttribute("userInfo", user);
        
        return "feedback";
    }
	

	@GetMapping("/channel-performance")
	public String channelPerformancePage(HttpSession session, Model model) {
	    Long userId = getSessionUserId(session);
	    if (userId == null) return "redirect:/login_test";

	    Long brandId = mypageService.getBrandId(userId);
	    model.addAttribute("brandId", brandId);
	    model.addAttribute("userInfo", mypageService.getUserInfo(userId));  // ← 추가
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
	public String temp3Page(Model model, HttpSession session) {
	    // 세션에서 userId 추출
	    Long userId = getSessionUserId(session);

	    // 헤더 등 공통 레이아웃에서 userInfo를 참조하므로 
	    // 로그인하지 않은 사용자에 대한 예외 처리가 필요합니다.
	    if (userId == null) {
	        return "redirect:/login";
	    }

	    // 가짜 데이터 대신 실제 유저 정보를 조회하여 바인딩
	    User user = userService.findById(userId);
	    model.addAttribute("userInfo", user);
	    
	    return "temp3";
	}
}