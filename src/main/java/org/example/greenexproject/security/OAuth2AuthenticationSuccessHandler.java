package org.example.greenexproject.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final OAuth2Service oauth2Service;
    
    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        log.info("OAuth2 authentication successful for user: {}", email);

        try {
            AuthResponse authResponse = oauth2Service.handleGoogleOAuth2Login(oauth2User, "google");

            // Redirect to frontend with JWT token
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth-callback")
                    .queryParam("token", authResponse.getToken())
                    .queryParam("userId", authResponse.getUserId())
                    .queryParam("email", authResponse.getEmail())
                    .queryParam("userType", authResponse.getUserType())
                    .build()
                    .toUriString();
            
            log.info("Redirecting OAuth2 user to frontend: {}", frontendUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();
            response.sendRedirect(errorUrl);
        }
    }
}
