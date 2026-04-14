package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * CSRF Cookie 설정 전용 SecurityFilterChain
 *
 * [목적]
 * SecurityConfig의 기본 HttpSessionCsrfTokenRepository는
 * 대용량 Thymeleaf 템플릿 렌더링 중 응답이 flush(commit)된 이후
 * CSRF 세션을 생성하려 할 때 아래 예외를 발생시킵니다.
 *
 *   IllegalStateException: Cannot create a session after the response has been committed
 *     → Thymeleaf SpringActionTagProcessor (th:action)
 *     → HttpSessionCsrfTokenRepository.saveToken()
 *     → ERR_INCOMPLETE_CHUNKED_ENCODING
 *
 * CookieCsrfTokenRepository는 세션 대신 쿠키에 토큰을 저장하므로
 * 응답 commit 타이밍과 무관하게 동작합니다.
 *
 * [구조]
 * Spring Security는 @Order가 낮을수록 먼저 매칭됩니다.
 * 이 FilterChain은 @Order(1)로 /content/** 경로를 먼저 처리하고,
 * 나머지는 SecurityConfig의 FilterChain(@Order 기본값)이 처리합니다.
 *
 * [SecurityConfig 수정 불필요]
 * 기존 SecurityConfig는 변경하지 않습니다.
 */
@Configuration
public class CsrfCookieConfig {

    @Bean
    @Order(1)  // SecurityConfig보다 우선 적용 (기본값은 @Order(2147483648) = 최하위)
    public SecurityFilterChain cookieCsrfFilterChain(HttpSecurity http) throws Exception {

        // CsrfTokenRequestAttributeHandler: Spring Security 6+ 권장 핸들러
        // th:action 등 Thymeleaf 폼에서 CSRF 토큰을 request attribute로 주입
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
            // 이 FilterChain이 처리할 경로 (content-generate-new 템플릿 포함)
            .securityMatcher("/content/**", "/mypage/**")

            .csrf(csrf -> csrf
                // 세션 대신 쿠키 기반 CSRF 토큰 저장소 사용
                // withHttpOnlyFalse(): JavaScript에서 토큰 읽기 허용 (th:action 등에 필요)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )

            .authorizeHttpRequests(auth -> auth
                // SecurityConfig와 동일하게 /content/** 전체 permitAll 유지
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
