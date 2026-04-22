package com.example.demo.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class EntryPointSaveFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(authentication.getPrincipal()));

        boolean isStaticResource =
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/images/") ||
                requestURI.startsWith("/image/") ||
                requestURI.startsWith("/favicon.ico");

        boolean isAuthRequest =
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/login") ||
                requestURI.startsWith("/logout");

        boolean isApiRequest = requestURI.startsWith("/api/");

        boolean isSaveTarget =
                "GET".equalsIgnoreCase(request.getMethod()) &&
                !isLoggedIn &&
                !isStaticResource &&
                !isAuthRequest &&
                !isApiRequest;

        if (isSaveTarget) {
            String fullUrl = (queryString == null || queryString.isBlank())
                    ? requestURI
                    : requestURI + "?" + queryString;

            request.getSession().setAttribute("entryPoint", fullUrl);
        }

        filterChain.doFilter(request, response);
    }
}