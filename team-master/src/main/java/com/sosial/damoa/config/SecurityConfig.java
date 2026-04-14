package com.sosial.damoa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN_TOKEN = "admin-token";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/customer-center",
                                "/customer-center.html",
                                "/inquiry",
                                "/inquiry.html",
                                "/admin",
                                "/admin.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/inquiries").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/notices").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/faqs").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new AdminTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(false);
            config.setExposedHeaders(List.of("*"));
            return config;
        };
    }

    static class AdminTokenFilter extends OncePerRequestFilter {

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String uri = request.getRequestURI();

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            if (
                    uri.equals("/") ||
                            uri.equals("/customer-center") ||
                            uri.equals("/customer-center.html") ||
                            uri.equals("/inquiry") ||
                            uri.equals("/inquiry.html") ||
                            uri.equals("/admin") ||
                            uri.equals("/admin.html") ||
                            uri.startsWith("/css/") ||
                            uri.startsWith("/js/") ||
                            uri.startsWith("/images/") ||
                            uri.equals("/favicon.ico") ||
                            uri.equals("/api/admin/login") ||
                            uri.equals("/api/public/inquiries") ||
                            uri.equals("/api/public/notices") ||
                            uri.equals("/api/public/faqs")
            ) {
                return true;
            }

            return !uri.startsWith("/api/admin/");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String token = request.getHeader("admin-token");

            if (token == null || token.isBlank()) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            if (!ADMIN_TOKEN.equals(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Unauthorized");
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}