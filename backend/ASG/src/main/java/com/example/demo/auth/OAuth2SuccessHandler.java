package com.example.demo.auth;

import com.example.demo.security.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // PrincipalDetails로 직접 캐스팅
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        
        Long userId = principal.getUser().getId();
        String userStatus = principal.getUser().getStatus().name();

        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userStatus", userStatus);

        log.info("OAuth2 로그인 성공: userId={}, status={}", userId, userStatus);

        if ("ACTIVE".equals(userStatus)) {
            response.sendRedirect("/dashboard");
        } else {
            response.sendRedirect("/signup");
        }
    }
}