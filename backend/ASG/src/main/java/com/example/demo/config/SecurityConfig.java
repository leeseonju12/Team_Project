package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	
	/*
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login**", "/error", "/instagram/**","/content/**","/feedback"
                		+ "").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/facebook/token", true) // 로그인 성공 시 이 주소로 이동!
            );
        return http.build();
    }*/
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        // 1. CSRF 보안 기능을 끕니다 (이게 범인입니다!)
	        .csrf(csrf -> csrf.disable())
	        
	        .authorizeHttpRequests(auth -> auth
	            // 2. 아까 고민하신 "무한 페이지" 해결책: 
	            // 로그인이 필요한 주소만 딱 집어서 막고, 나머지는 다 열어줍니다.
	            .requestMatchers("/admin/**", "/mypage/**").authenticated() 
	            .anyRequest().permitAll() 
	        )
	        .oauth2Login(oauth2 -> oauth2
	            .defaultSuccessUrl("/facebook/token", true)
	        );
	    return http.build();
	}
}