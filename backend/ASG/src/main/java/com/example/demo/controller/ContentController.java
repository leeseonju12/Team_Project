package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.domain.enums.IndustryType;
import com.example.demo.domain.user.entity.User;
import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.dto.myPage.ContentSettingsResponse;
import com.example.demo.security.PrincipalDetails;
import com.example.demo.service.ContentService;
import com.example.demo.service.auth.UserService;
import com.example.demo.service.myPage.MypageService;

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

    private Long getLoginUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @GetMapping("/generate")
    public String showGeneratePage(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                   Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("defaultTab", "instagram");
        model.addAttribute("currentMenu", "content-generate");

        addCommonAttributes(principalDetails, model);

        return "index";
    }

    @PostMapping("/generate")
    public String generate(@AuthenticationPrincipal PrincipalDetails principalDetails,
                           @ModelAttribute ContentRequest request,
                           Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        request.setUserId(userId);

        List<SnsResult> results = geminiService.generateAllSnsContent(request);

        model.addAttribute("results", results);
        model.addAttribute("req", request);
        model.addAttribute("currentMenu", "content-generate");

        addCommonAttributes(principalDetails, model);

        return "index";
    }

    // ── 공통 모델 주입 ───────────────────────────────────────
    private void addCommonAttributes(PrincipalDetails principalDetails, Model model) {
        Long userId = getLoginUserId(principalDetails);

        // 1. userInfo
        if (userId != null) {
            try {
                User user = userService.findById(userId);
                model.addAttribute("userInfo", user);
            } catch (Exception e) {
                log.warn("userInfo 조회 실패. userId={}", userId, e);
                model.addAttribute("userInfo", null);
            }
        } else {
            model.addAttribute("userInfo", null);
        }

        // 2. businessCategory
        model.addAttribute("businessCategory", "");
        if (userId != null) {
            try {
                User user = userService.findById(userId);
                if (user != null && user.getBusinessCategory() != null) {
                    
                    // ✅ 수정 포인트: User의 한글 문자열을 Enum 코드로 변환해서 넘김
                    String categoryCode = IndustryType.fromDescription(user.getBusinessCategory()).name();
                    model.addAttribute("businessCategory", categoryCode);
                    
                }
            } catch (Exception e) {
                log.warn("businessCategory 조회 실패. userId={}", userId, e);
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
                log.warn("contentSettings 조회 실패. userId={}", userId, e);
            }
        }
    }
}