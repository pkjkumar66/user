package com.example.demo.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Example: redirect to frontend home page
        response.sendRedirect("/api/users/login-success");

        // Alternatively: generate JWT, persist user, etc.
        // You can extract user info:
        // OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        // Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
    }
}

