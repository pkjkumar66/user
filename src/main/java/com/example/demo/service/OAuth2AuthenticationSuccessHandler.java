package com.example.demo.service;

import com.example.demo.dto.UserResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
//    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl = "app.frontend.url:http://localhost:3000";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String providerId = oauthUser.getAttribute("sub") != null ? oauthUser.getAttribute("sub") : oauthUser.getAttribute("id");
        String email = oauthUser.getAttribute("email");

        UserResponse userResponse = userService.oauthLogin(provider, providerId, email);
        String token = userResponse.getToken();

        // decide whether to redirect with token in query param or set as HttpOnly cookie
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", token)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

