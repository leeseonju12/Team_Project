package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class CsrfCookieConfig {

    /**
     * [Chain 1] 고객센터 API — CSRF disable
     * admin.html / customer-center.html 의 fetch() 요청은
     * CSRF 토큰 없이 JSON을 보내므로 이 경로들은 CSRF를 끕니다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain customerCenterApiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/api/admin/**",
                "/api/public/**",
                "/api/inquiry"
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * [Chain 2] content / mypage — CookieCsrfTokenRepository
     * Thymeleaf 폼 + fetch() 혼용 경로는 쿠키 기반 CSRF 유지
     */
    @Bean
    @Order(2)
    public SecurityFilterChain cookieCsrfFilterChain(HttpSecurity http) throws Exception {

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        http
            .securityMatcher("/content/**", "/mypage/**", "/customer-center/**")
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}