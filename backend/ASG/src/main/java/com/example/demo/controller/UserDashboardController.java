package com.example.demo.controller;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.security.PrincipalDetails;
import com.example.demo.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
        if (principal == null) {
            return "redirect:/login_test";
        }

        // PrincipalDetails의 user 대신 DB에서 최신 상태 조회 ← 핵심 변경
        User user = userService.findById(principal.getUser().getId());

        if (user.getStatus() != UserStatus.ACTIVE) {
            return "redirect:/signup";
        }

        model.addAttribute("user", user);
        model.addAttribute("loginUser", user);

        log.info("대시보드 접근: userId={}, nickname={}", user.getId(), user.getNickname());
        return "dashboard";
    }
}