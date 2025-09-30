package com.example.demo.config.security;

import com.example.demo.dao.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Extract OAuth2 user info
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String provider = oauthToken.getAuthorizedClientRegistrationId(); // e.g., "google"
        String providerId = (String) attributes.get("sub"); // Google unique ID

        // Create or get user
        Optional<User> userOpt = userRepository.findByProviderAndProviderId(provider, providerId);

        String token = jwtUtil.generateToken(userOpt.orElse(User.builder().build()));

        // Redirect to frontend with token (example)
        response.sendRedirect("/api/auth/sso/success?token=" + token);
    }
}

