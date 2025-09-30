package com.example.demo.config.security;

import com.example.demo.constant.ApiPaths;
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
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google"
        String providerId = (String) attributes.get("sub"); // Google unique ID
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name");

        // Find existing user or create new one
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .username(username)
                            .provider(User.AuthProvider.GOOGLE)
                            .providerId(providerId)
                            // You can set a random password or null since login is OAuth2
                            .password(UUID.randomUUID().toString())
                            .build();
                    return userRepository.save(newUser);
                });

        updateLastLogin(user.getUsername());
        String token = jwtUtil.generateToken(user);

        // Redirect to frontend with token (example)
        response.sendRedirect(ApiPaths.FULL_SSO_SUCCESS + "?token=" + token);
    }

    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(Instant.now());
            userRepository.save(user);
        }
    }
}

