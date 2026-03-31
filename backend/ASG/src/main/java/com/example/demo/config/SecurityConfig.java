package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.example.demo.security.CustomLogoutSuccessHandler;
import com.example.demo.security.EntryPointSaveFilter;
import com.example.demo.security.NoCacheHeaderFilter;
import com.example.demo.security.OAuth2LoginFailureHandler;
import com.example.demo.security.OAuth2LoginSuccessHandler;
import com.example.demo.service.auth.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
	private final NoCacheHeaderFilter noCacheHeaderFilter;
	private final EntryPointSaveFilter entryPointSaveFilter;
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        // 1. CSRF 설정: API 호출 시 403 에러 방지를 위해 예외 경로 지정
	        .csrf(csrf -> csrf
	            .ignoringRequestMatchers(
	                "/signup/complete",
	                "/api/feedbacks/**", 
	                "/api/sns/**", 
	                "/search-test"
	            )
	        )
	        
	        // 2. 요청 권한 설정
	        .authorizeHttpRequests(auth -> auth
	            // 공통 허용 경로
	            .requestMatchers("/", "/login_test", "/logout-success", "/error", "/landing-page").permitAll()
	            .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
	            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
	            
	            // 피드백 및 SNS API 관련 (Illellu님 작업분)
	            .requestMatchers("/feedback.html", "/api/feedbacks/**", "/feedback", "/feedbacks").permitAll()
	            .requestMatchers("/content/**", "/api/sns/**", "/api/analytics/**").permitAll()
	            .requestMatchers("/search-test", "/imgSearchTest.html", "/pexels_test.html", "/sns/pexels").permitAll()
	            .requestMatchers("/channel-performance", "/api/channel-performance").permitAll()
	            
	            // 회원가입 관련
	            .requestMatchers("/signup", "/signup/complete").permitAll()
	            
	            // 인증이 필요한 페이지들
	            .requestMatchers("/signup_form", "/dashboard", "/admin/**", "/mypage/**").authenticated()
	            
	            // 나머지는 일단 모두 허용 (개발 편의를 위해 permitAll 사용하거나, 보안을 위해 authenticated 사용)
	            .anyRequest().permitAll() 
	        )
	        
	        // 3. OAuth2 로그인 설정
	        .oauth2Login(oauth2 -> oauth2
	            .loginPage("/login_test")
	            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
	            .successHandler(oAuth2LoginSuccessHandler)
	            .failureHandler(oAuth2LoginFailureHandler)
	        )
	        
	        // 4. 예외 처리: 인증되지 않은 사용자 처리
	        .exceptionHandling(e -> e
	            .authenticationEntryPoint((request, response, authException) -> {
	                response.sendRedirect("/login_test");
	            })
	        )
	        
	        // 5. 로그아웃 설정
	        .logout(logout -> logout
	            .logoutUrl("/logout-success")
	            .logoutSuccessHandler(customLogoutSuccessHandler)
	            .invalidateHttpSession(true)
	            .clearAuthentication(true)
	            .deleteCookies("JSESSIONID")
	        )
	        
	        // 6. 필터 추가
	        .addFilterBefore(entryPointSaveFilter, SecurityContextHolderFilter.class)
	        .addFilterAfter(noCacheHeaderFilter, SecurityContextHolderFilter.class);

	    return http.build();
	}

}