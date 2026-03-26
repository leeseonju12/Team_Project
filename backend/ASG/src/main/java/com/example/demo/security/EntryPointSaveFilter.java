package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class EntryPointSaveFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        // /oauth2/authorization/** 요청에서 entry 파라미터 세션 저장
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/oauth2/authorization/")) {
            String entry = request.getParameter("entry");
            if (entry != null) {
                request.getSession().setAttribute("entryPoint", entry);
            }
        }

        filterChain.doFilter(request, response);
    }
}