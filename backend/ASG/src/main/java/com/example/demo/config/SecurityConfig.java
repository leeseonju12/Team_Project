package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.example.demo.security.EntryPointSaveFilter;
import com.example.demo.security.JwtAuthenticationFilter;
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
    private final NoCacheHeaderFilter noCacheHeaderFilter;
    private final EntryPointSaveFilter entryPointSaveFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// 1. CSRF 설정: API 호출 시 403 에러 방지를 위해 예외 경로 지정
				.csrf(csrf -> csrf.ignoringRequestMatchers("/signup/complete", "/api/feedbacks/**", "/api/sns/**",
						"/search-test", "/api/v1/content/generate", "/api/v1/content/**", "/api/v1/ai/**",
						"/api/posts/**", "/calendar/**", "/api/auth/logout"))
				
				.sessionManagement(session -> session
		                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		            )

				.authorizeHttpRequests(auth -> auth
						// 1. 모든 요청을 허용하도록 변경 (기존 리스트는 유지하되 마지막을 permitAll로)
						.requestMatchers("/", "/login_test", "/logout-success", "/error", "/landing-page", "/mypage")
						.permitAll().requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
						.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
						.requestMatchers("/feedback.html", "/api/feedbacks/**", "/feedback").permitAll()
						.requestMatchers("/content/**", "/api/sns/**").permitAll().requestMatchers("/api/analytics/**")
						.permitAll().requestMatchers("/api/v1/content/**").permitAll()
						.requestMatchers("/signup", "/signup/complete").permitAll()
						.requestMatchers("/channel-performance", "/api/channel-performance").permitAll()
						.requestMatchers("/login").permitAll()
						.requestMatchers("/api/auth/refresh", "/api/auth/logout").permitAll()
						.requestMatchers("/api/secure/**").authenticated().requestMatchers("/dashboard").authenticated()

						// 아래 인증이 필요한 부분들을 모두 permitAll()로 수정하거나 주석 처리합니다.
						// .requestMatchers("/signup_form").authenticated()
						// .requestMatchers("/dashboard").authenticated()
						// .requestMatchers("/admin/**", "/mypage/**").authenticated()

						// 2. 핵심: 어떤 요청이든 인증 없이 허용함
						.anyRequest().permitAll()
				)
				// 3. 인증 실패 시 로그인 페이지로 리다이렉트하는 로직을 주석 처리
				/*
				 * .exceptionHandling(e -> e .authenticationEntryPoint((request, response,
				 * authException) -> { response.sendRedirect("/login"); }) )
				 */
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/login")
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(oAuth2LoginSuccessHandler)
						.failureHandler(oAuth2LoginFailureHandler)
					)
				
				.logout(logout -> logout.disable())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(entryPointSaveFilter, SecurityContextHolderFilter.class)
				.addFilterAfter(noCacheHeaderFilter, SecurityContextHolderFilter.class);

		return http.build();
	}

}