package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.dto.SignupRequest;
import com.example.demo.security.PrincipalDetails;
import com.example.demo.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignupController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(@AuthenticationPrincipal PrincipalDetails principal,
                             Model model) {

        // 비로그인 상태 → 소셜 로그인 버튼이 있는 회원가입 페이지 그대로 보여줌
        if (principal == null) {
            return "signup"; // 
        }

        // 로그인 상태 → DB에서 최신 상태 조회
        User user = userService.findById(principal.getUser().getId());

        // 이미 가입 완료된 사용자 → 대시보드로
        if (user.getStatus() == UserStatus.ACTIVE) {
            return "redirect:/dashboard";
        }

        // 소셜 로그인 완료 후 약관/정보입력 단계
        model.addAttribute("email", user.getEmail());
        model.addAttribute("name", user.getName());
        model.addAttribute("provider", user.getProvider());
        return "signup";
    }

    @PostMapping("/signup/complete")
    public String completeSignup(@ModelAttribute SignupRequest dto,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 정보 없음: userId={}", userId);
            return "redirect:/login";
        }

        if (!dto.isTermsAgreed() || !dto.isPrivacyAgreed()) {
            redirectAttributes.addFlashAttribute("error", "필수 약관에 동의해주세요.");
            return "redirect:/signup";
        }

        if (userService.isNicknameDuplicated(dto.getNickname())) {
            redirectAttributes.addFlashAttribute("error", "이미 사용중인 닉네임입니다.");
            return "redirect:/signup";
        }

        userService.completeSignup(userId, dto);
        session.setAttribute("userStatus", "ACTIVE");
        log.info("회원가입 완료: userId={}", userId);
        return "redirect:/dashboard";
    }
}