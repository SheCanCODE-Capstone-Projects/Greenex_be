package org.example.greenexproject.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.LoginRequest;
import org.example.greenexproject.dto.request.RegisterRequest;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.exception.UnauthorizedException;
import org.example.greenexproject.model.entity.*;
import org.example.greenexproject.model.enums.*;
import org.example.greenexproject.repository.*;
import org.example.greenexproject.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final SystemUserRepository systemUserRepository;
    private final AdminUserRepository adminUserRepository;
    private final CompanyUserRepository companyUserRepository;
    private final WasteCompanyRepository wasteCompanyRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) throws BadRequestException {

        if (systemUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (systemUserRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }


        SystemUser systemUser = SystemUser.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .status(UserStatus.ACTIVE)
                .build();

        systemUser = systemUserRepository.save(systemUser);


        if (request.getUserType() == UserType.ADMIN) {
            AdminUser adminUser = AdminUser.builder()
                    .systemUser(systemUser)
                    .status(UserStatus.ACTIVE)
                    .build();
            adminUserRepository.save(adminUser);
        }


        String token = jwtTokenProvider.generateToken(
                systemUser.getId(),
                systemUser.getEmail(),
                systemUser.getUserType().name(),
                null
        );

        return AuthResponse.builder()
                .token(token)
                .userId(systemUser.getId())
                .email(systemUser.getEmail())
                .fullName(systemUser.getFullName())
                .userType(systemUser.getUserType().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        SystemUser systemUser = systemUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(),
                systemUser.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }


        if (systemUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }


        UUID companyId = null;
        if (systemUser.getUserType() == UserType.COMPANY_MANAGER ||
                systemUser.getUserType() == UserType.COMPANY_DRIVER) {
            companyId = companyUserRepository.findBySystemUser_Id(systemUser.getId())
                    .map(cu -> cu.getWasteCompany().getId())
                    .orElse(null);
        }


        String token = jwtTokenProvider.generateToken(
                systemUser.getId(),
                systemUser.getEmail(),
                systemUser.getUserType().name(),
                companyId
        );

        return AuthResponse.builder()
                .token(token)
                .userId(systemUser.getId())
                .email(systemUser.getEmail())
                .fullName(systemUser.getFullName())
                .userType(systemUser.getUserType().name())
                .companyId(companyId)
                .build();
    }

    public void registerCompany(UUID managerId, CompanyRegistrationRequest request) throws BadRequestException {

        SystemUser manager = systemUserRepository.findById(managerId)
                .orElseThrow(() -> new BadRequestException("Manager user not found"));

        if (manager.getUserType() != UserType.COMPANY_MANAGER) {
            throw new BadRequestException("Only company managers can register companies");
        }
        if (wasteCompanyRepository.existsByName(request.getCompanyName())) {
            throw new BadRequestException("Company name already exists");
        }
    }
}