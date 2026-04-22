package com.example.demo.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.domain.user.entity.RefreshToken;
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("entryPoint");
        }

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


        if (user.getStatus() == UserStatus.ACTIVE) {
            response.sendRedirect("/mypage");
        } else {
            response.sendRedirect("/signup?social_login=success");
        }
    }
}