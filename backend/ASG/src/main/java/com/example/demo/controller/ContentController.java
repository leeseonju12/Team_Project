// 변경 후 전체
package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

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
import com.example.demo.dto.myPage.ContentSettingsResponse;
import com.example.demo.service.ContentService;
import com.example.demo.service.auth.UserService;
import com.example.demo.service.myPage.MypageService;

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
    private final MypageService mypageService;

    private Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @GetMapping("/generate")
    public String showGeneratePage(HttpSession session, Model model, Principal principal) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        // HEAD branch specific view state
        model.addAttribute("defaultTab", "instagram");
        
        // Refactored common attributes method from the incoming branch
        addCommonAttributes(session, model, principal);
        
        return "index";
    }

    @PostMapping("/generate")
    public String generate(HttpSession session, @ModelAttribute ContentRequest request,
                           Model model, Principal principal) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        
        request.setUserId(userId);

        List<SnsResult> results = geminiService.generateAllSnsContent(request);

        model.addAttribute("results", results);
        model.addAttribute("req", request);
        
        addCommonAttributes(session, model, principal);
        
        return "index";
    }

    // ── 공통 모델 주입 ───────────────────────────────────────
    private void addCommonAttributes(HttpSession session, Model model, Principal principal) {
        // 1. userInfo — 세션 기반
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            try {
                User user = userService.findById(userId);
                model.addAttribute("userInfo", user);
            } catch (Exception e) {
                log.warn("userInfo 조회 실패. userId={}", userId);
                model.addAttribute("userInfo", null);
            }
        } else {
            model.addAttribute("userInfo", null);
        }

        // 2. businessCategory
        model.addAttribute("businessCategory", "");
        if (principal != null) {
            try {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
                OAuth2User oauthUser = oauthToken.getPrincipal();
                String email = (String) oauthUser.getAttributes().get("email");
                User user = userService.findByEmail(email);
                if (user != null && user.getBusinessCategory() != null) {
                    model.addAttribute("businessCategory", user.getBusinessCategory());
                }
            } catch (Exception e) {
                log.error("businessCategory 주입 중 오류. principal={}", principal.getName(), e);
            }
        }

        // 3. contentSettings — useDefaultMode ON일 때만 주입
        model.addAttribute("contentSettings", null);
        if (userId != null) {
            try {
                ContentSettingsResponse settings = mypageService.getContentSettings(userId);
                if (settings != null && Boolean.TRUE.equals(settings.getUseDefaultMode())) {
                    model.addAttribute("contentSettings", settings);
                }
            } catch (Exception e) {
                log.warn("contentSettings 조회 실패. userId={}", userId);
            }
        }
    }
}