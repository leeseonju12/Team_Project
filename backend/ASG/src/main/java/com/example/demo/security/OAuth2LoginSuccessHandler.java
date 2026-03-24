package com.example.demo.security;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();

        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userStatus", user.getStatus().name());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            securityContext
        );

        // 세션에서 진입 경로 확인 ← 핵심
        String entryPoint = (String) session.getAttribute("entryPoint");

        if ("signup".equals(entryPoint)) {
            // 회원가입 페이지에서 진입 → 신규/기존 무관하게 약관 페이지로
            session.removeAttribute("entryPoint");
            if (user.getStatus() == UserStatus.ACTIVE) {
                // 이미 가입된 사용자가 회원가입 페이지에서 로그인 시 대시보드로
                response.sendRedirect("/dashboard");
            } else {
                response.sendRedirect("/signup?social_login=success");
            }
        } else {
            // 로그인 페이지에서 진입
            if (user.getStatus() == UserStatus.ACTIVE) {
                response.sendRedirect("/dashboard");
            } else {
                // 미완성 가입자가 로그인 페이지에서 로그인 시 회원가입으로
                response.sendRedirect("/signup?social_login=success");
            }
        }
    }
} 