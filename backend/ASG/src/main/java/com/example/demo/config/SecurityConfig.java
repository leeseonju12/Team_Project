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

        .csrf(csrf -> csrf
                .ignoringRequestMatchers("/signup/complete",
                		"/api/feedbacks/**", "/api/sns/**", "/search-test"
                		
                		
                		)
                
            )
        
        .authorizeHttpRequests(auth -> auth
        	    .requestMatchers("/", "/login_test", "/logout-success", "/error", "/landing-page").permitAll()
        	    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
        	    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
        	    .requestMatchers("/feedback.html", "/api/feedbacks/**", "/feedback","/feedbacks").permitAll()
        	    .requestMatchers("/content/**", "/api/sns/**").permitAll()
                .requestMatchers("/api/analytics/**").permitAll()
                .requestMatchers("/search-test", "/imgSearchTest.html").permitAll()
               
        	    .requestMatchers("/signup", "/signup/complete").permitAll()
        	    .requestMatchers("/signup_form").authenticated() // ← 추가 (로그인 후에만 접근)
        	    .requestMatchers("/dashboard").authenticated()
        	    .requestMatchers("/admin/**", "/mypage/**").authenticated()
        	    .anyRequest().authenticated()
        	)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login_test")          // /login → /login_test 로 수정
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
            )
            // formLogin 제거 (소셜 로그인 전용)
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login_test"); // 동일하게 유지
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout-success")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            .addFilterBefore(entryPointSaveFilter, SecurityContextHolderFilter.class)
            .addFilterAfter(noCacheHeaderFilter, SecurityContextHolderFilter.class);

        return http.build();
    }
}