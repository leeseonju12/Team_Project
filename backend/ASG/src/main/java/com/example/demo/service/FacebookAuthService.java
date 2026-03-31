package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacebookAuthService {

//     Sapplication.yml에 있는 앱 ID와 시크릿 코드를 가져옵니다.
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0";

    /**
     * OAuth2 로그인으로 얻은 단기 토큰을 60일짜리 장기 토큰으로 교환합니다.
     */
    public String exchangeForLongLivedToken(String shortLivedToken) {
        // Graph API 요청 URL 만들기
    	String url = UriComponentsBuilder.fromUriString(GRAPH_API_BASE_URL + "/oauth/access_token")
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", shortLivedToken)
                .toUriString();

        try {
            // Meta 서버로 GET 요청 보내기
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 응답받은 JSON에서 새로 발급된 장기 access_token만 쏙 빼서 반환
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            // 실제 프로젝트에서는 로거(log.error)를 사용하거나 사용자 정의 예외를 던지는 것이 좋습니다.
            System.err.println("장기 토큰 발급 중 에러 발생: " + e.getMessage());
            throw new RuntimeException("페이스북 장기 토큰 교환에 실패했습니다.", e);
        }
        
        return null;
    }
    

    
}
