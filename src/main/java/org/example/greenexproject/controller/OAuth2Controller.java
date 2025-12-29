package org.example.greenexproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.service.OAuth2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oauth2Service;

    @GetMapping("/success")
    public ResponseEntity<AuthResponse> oauthSuccess(
            @AuthenticationPrincipal OAuth2User oauth2User) {

        if (oauth2User == null) {
            log.error("OAuth2User is null");
            return ResponseEntity.status(401).build();
        }

        String email = oauth2User.getAttribute("email");
        log.info("OAuth2 authentication successful for user: {}", email);

        AuthResponse response = oauth2Service.handleGoogleOAuth2Login(oauth2User, "google");
        return ResponseEntity.ok(response);
    }
}
