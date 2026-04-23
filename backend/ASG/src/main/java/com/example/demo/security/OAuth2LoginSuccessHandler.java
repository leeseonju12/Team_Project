package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.service.auth.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getStatus().name(),
                user.getRole()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.replaceRefreshToken(
                user.getId(),
                refreshToken,
                refreshExpirationMs
        );

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshExpirationMs / 1000));
        response.addCookie(refreshCookie);

        // accessToken도 쿠키로 쓸 경우
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(cookieSecure);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 30); // 예: 30분
        response.addCookie(accessCookie);

        if (user.getStatus() != UserStatus.ACTIVE) {
            response.sendRedirect("/signup?social_login=success");
            return;
        }
        
        // ↓ 여기에 추가 (ACTIVE 계정만 발급)
        if ("ROLE_ADMIN".equals(user.getRole())) {
            Cookie adminAccessCookie = new Cookie("admin_access_token", accessToken);
            adminAccessCookie.setHttpOnly(false);
            adminAccessCookie.setSecure(cookieSecure);
            adminAccessCookie.setPath("/admin");
            adminAccessCookie.setMaxAge(60 * 30);
            response.addCookie(adminAccessCookie);
        }

     // 변경 후
        if ("ROLE_ADMIN".equals(user.getRole())) {
            response.sendRedirect("/admin");
            return;
        }

        String redirectUrl = "/mypage";

        HttpSession session = request.getSession(false);
        if (session != null) {
            String entryPoint = (String) session.getAttribute("entryPoint");
            if (entryPoint != null && !entryPoint.isBlank()) {
                redirectUrl = entryPoint;
            }
            session.removeAttribute("entryPoint");
        }

        response.sendRedirect(redirectUrl);
    }
}