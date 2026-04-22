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
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/signup/complete",
                "/api/feedbacks/**",
                "/api/sns/**",
                "/search-test",
                "/api/v1/content/generate",
                "/api/v1/content/**",
                "/api/v1/ai/**",
                "/api/posts/**",
                "/calendar/**",
                "/api/auth/logout"
            ))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/login_test",
                    "/logout-success",
                    "/error",
                    "/landing-page",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/signup",
                    "/signup/complete",
                    "/api/auth/refresh",
                    "/api/auth/logout"
                ).permitAll()

                .requestMatchers("/mypage", "/mypage/**").authenticated()
                .requestMatchers("/channel-performance").authenticated()
                .requestMatchers("/api/channel-performance").authenticated()
                .requestMatchers("/dashboard").authenticated()

                .anyRequest().permitAll()
            )
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