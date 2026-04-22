package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.security.PrincipalDetails;
import com.example.demo.service.auth.UserService;
import com.example.demo.service.myPage.MypageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final MypageService mypageService;

    private Long getLoginUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @GetMapping("/feedbacks")
    public String feedbackPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userInfo", userService.findById(userId));
        model.addAttribute("currentMenu", "feedback");

        return "feedback";
    }

    @GetMapping("/channel-performance")
    public String channelPerformancePage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        Long brandId = mypageService.getBrandId(userId);

        model.addAttribute("brandId", brandId);
        model.addAttribute("userInfo", mypageService.getUserInfo(userId));
        model.addAttribute("currentMenu", "performance");

        return "channel-performance";
    }

    @GetMapping("/search-test")
    public String searchTestPage() {
        return "forward:/imgSearchTest.html";
    }

    @GetMapping("/sns/pexels")
    public String showPexelsPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userInfo", mypageService.getUserInfo(userId));
        return "pexels";
    }

    @GetMapping("/temp3")
    public String temp3Page(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        Long userId = getLoginUserId(principalDetails);
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userInfo", userService.findById(userId));
        return "temp3";
    }
    
    @GetMapping("/admin")
    public String adminPage(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) return "redirect:/login";
        return "admin";
    }

    @GetMapping("/customer-center")
    public String customerCenterPage() {
        return "customer-center";
    }
}