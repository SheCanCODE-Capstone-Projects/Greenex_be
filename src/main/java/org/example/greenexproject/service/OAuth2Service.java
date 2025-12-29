package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.model.entity.SystemUser;
import org.example.greenexproject.model.enums.UserType;
import org.example.greenexproject.repository.SystemUserRepository;
import org.example.greenexproject.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {
    private final SystemUserRepository systemUserRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse handleGoogleOAuth2Login(OAuth2User oauth2User, String registrationId) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email not provided by Google");
        }

        // Check if user already exists
        Optional<SystemUser> existingUser = systemUserRepository.findByEmail(email);

        SystemUser user;
        if (existingUser.isPresent()) {
            user = existingUser.get();

            // Only allow CITIZEN users to login via OAuth
            if (user.getUserType() != UserType.CITIZEN) {
                throw new BadRequestException("Google OAuth is only available for citizen accounts");
            }

            log.info("Existing citizen user logged in via Google OAuth: {}", email);
        } else {
            // Create new citizen account
            user = SystemUser.builder()
                    .email(email)
                    .fullName(name != null ? name : email)
                    .phone("N/A") // Will be updated when creating household
                    .passwordHash("OAUTH2_GOOGLE") // Marker for OAuth users
                    .userType(UserType.CITIZEN)
                    .build();

            user = systemUserRepository.save(user);
            log.info("New citizen user created via Google OAuth: {}", email);
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUserType().name(),
                null
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userType(user.getUserType().name())
                .build();
    }
}
