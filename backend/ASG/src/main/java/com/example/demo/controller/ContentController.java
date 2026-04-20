package com.example.demo.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.domain.user.entity.User;
import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.service.ContentService;
import com.example.demo.service.auth.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

	private final ContentService geminiService;
	private final UserService userService;
	
	
	
    private Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @GetMapping("/generate")
    public String showGeneratePage(Model model, Principal principal, HttpSession session) {
    	
    	model.addAttribute("defaultTab", "instagram");
    	
        // 세션에서 userId 추출
        Long userId = getSessionUserId(session);

        // 세션 정보가 없을 경우 처리 (예: 로그인 페이지 리다이렉트)
        if (userId == null) {
            return "redirect:/login";
        }

        // UserService를 통해 DB에서 실제 유저 정보를 조회 (User 엔티티 또는 DTO 반환)
        // findById 등의 메서드가 구현되어 있다고 가정
        User user = userService.findById(userId);

        // 모델에 실제 유저 정보 바인딩
        model.addAttribute("userInfo", user);
        
        // 비즈니스 카테고리 로직 수행
        addBusinessCategory(model, principal);
        
        return "index";
    }

	@PostMapping("/generate")
	public String generate(HttpSession session, @ModelAttribute ContentRequest request, Model model, Principal principal) {

		Long userId = (Long) session.getAttribute("userId");
		request.setUserId(userId); // null이면 ContentService에서 BRAND_ID=1L로 fallback

		List<SnsResult> results = geminiService.generateAllSnsContent(request);

		model.addAttribute("results", results);
		model.addAttribute("req", request);
		addBusinessCategory(model, principal);
		return "index";
	}

	private void addBusinessCategory(Model model, Principal principal) {
		model.addAttribute("businessCategory", "");
		if (principal == null)
			return;

		try {
			// ✅ OAuth2 attributes에서 email 직접 추출
			OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
			OAuth2User oauthUser = oauthToken.getPrincipal();
			String email = (String) oauthUser.getAttributes().get("email");

			log.info("OAuth2 email = {}", email); // 확인용

			User user = userService.findByEmail(email);

			if (user == null) {
				log.warn("사용자를 찾을 수 없습니다. email={}", email);
				return;
			}

			String businessCategory = user.getBusinessCategory();
			if (businessCategory == null || businessCategory.isBlank()) {
				log.warn("사용자 업종 정보가 비어 있습니다. email={}", email);
				return;
			}

			model.addAttribute("businessCategory", businessCategory);

		} catch (Exception e) {
			log.error("businessCategory 주입 중 오류 발생. principal={}", principal.getName(), e);
		}
	}
}