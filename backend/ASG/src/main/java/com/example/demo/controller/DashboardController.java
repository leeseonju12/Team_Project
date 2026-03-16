package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RegisteredOAuth2AuthorizedClient("facebook") OAuth2AuthorizedClient authorizedClient) {
        
        // 1. 로그인한 사용자의 기본 정보 확인
        String name = oauth2User.getAttribute("name");
        
        // 2. 핵심! 인스타그램 API 호출에 사용할 액세스 토큰 추출
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return "<h1>연동 성공! 환영합니다, " + name + "님</h1>" +
               "<p>이것이 인스타그램에 글을 쓸 수 있는 마법의 키입니다:</p>" +
               "<textarea rows='5' cols='100'>" + accessToken + "</textarea>";
    }
}