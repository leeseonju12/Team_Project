package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.FacebookApiService;
import com.example.demo.service.FacebookAuthService;

@RestController
public class FacebookController {

    private final FacebookAuthService facebookAuthService;
    private final FacebookApiService facebookApiService;

    // Service 의존성 주입
    public FacebookController(FacebookAuthService facebookAuthService, FacebookApiService facebookApiService) {
        this.facebookAuthService = facebookAuthService;
        this.facebookApiService = facebookApiService;
    }

    @GetMapping("/facebook/token")
    public String getFacebookToken(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient("facebook") OAuth2AuthorizedClient authorizedClient) {
        
        String userName = principal.getAttribute("name");
        
        // 1. Spring Security가 받아온 1~2시간짜리 단기 토큰 추출
        String shortLivedToken = authorizedClient.getAccessToken().getTokenValue();

        // 2. 서비스 클래스를 호출해 60일짜리 장기 토큰으로 교환!
        String longLivedToken = facebookAuthService.exchangeForLongLivedToken(shortLivedToken);

        /* * 💡 실제 서비스에서는 이 longLivedToken을 
         * 데이터베이스(예: Member 테이블 또는 SocialToken 테이블)에 
         * 사용자 정보와 함께 저장(UPDATE)해 두어야 합니다!
         */

        return "<h1>안녕하세요, " + userName + "님!</h1>" +
               "<p><b>단기 토큰:</b> <br>" + shortLivedToken + "</p>" +
               "<hr>" +
               "<p><b>장기 토큰 (60일 유효, DB 저장용):</b> <br><b style='color:blue;'>" + longLivedToken + "</b></p>";
    }
    
}