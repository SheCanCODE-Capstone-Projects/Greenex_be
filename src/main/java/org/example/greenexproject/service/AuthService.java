package org.example.greenexproject.service;

import com.cloudinary.Cloudinary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.CompanyRegistrationRequest;
import org.example.greenexproject.dto.request.LoginRequest;
import org.example.greenexproject.dto.request.RegisterRequest;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.exception.UnauthorizedException;
import org.example.greenexproject.model.entity.*;
import org.example.greenexproject.model.enums.*;
import org.example.greenexproject.repository.*;
import org.example.greenexproject.security.JwtTokenProvider;
import org.example.greenexproject.service.EmailService;
import org.example.greenexproject.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final CloudinaryService cloudinaryService;


    private final OtpService otpService;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) throws BadRequestException {

        if (systemUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (systemUserRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }
        if (request.getPhone().length() != 10) {
            throw new BadRequestException("Phone number must be exactly 10 digits");
        }

        SystemUser systemUser = SystemUser.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .status(UserStatus.PENDING)
                .build();

        systemUser = systemUserRepository.save(systemUser);

        if (request.getUserType() == UserType.ADMIN) {
            AdminUser adminUser = AdminUser.builder()
                    .systemUser(systemUser)
                    .status(UserStatus.PENDING)
                    .build();
            adminUserRepository.save(adminUser);
        }

        try {
            // Generate OTP linked to email and 5-minute expiry
            String otp = otpService.generateOtp(systemUser.getEmail());
            emailService.sendOtpEmail(systemUser.getEmail(), otp);
        } catch (Exception e) {
            System.err.println("Warning: OTP/email sending failed: " + e.getMessage());
        }

        return AuthResponse.builder()
                .userId(systemUser.getId())
                .email(systemUser.getEmail())
                .fullName(systemUser.getFullName())
                .userType(systemUser.getUserType().name())
                .message("Registration successful. OTP sent to email.")
                .build();
    }

    // New method: verify OTP only
    public boolean verifyOtp(String otp) {
        String email = otpService.validateOtp(otp);
        if (email == null) {
            return false; // invalid or expired OTP
        }

        // Activate user
        systemUserRepository.findByEmail(email).ifPresent(user -> {
            user.setStatus(UserStatus.ACTIVE);
            systemUserRepository.save(user);

            if (user.getUserType() == UserType.ADMIN) {
                adminUserRepository.findBySystemUser_Id(user.getId())
                        .ifPresent(admin -> {
                            admin.setStatus(UserStatus.ACTIVE);
                            adminUserRepository.save(admin);
                        });
            }
        });

        return true;
    }

    public void activateUserByEmail(String email) throws BadRequestException {
        SystemUser user = systemUserRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        systemUserRepository.save(user);

        if (user.getUserType() == UserType.ADMIN) {
            adminUserRepository.findBySystemUser_Id(user.getId())
                    .ifPresent(admin -> {
                        admin.setStatus(UserStatus.ACTIVE);
                        adminUserRepository.save(admin);
                    });
        }
    }

    public AuthResponse login(LoginRequest request) {
        SystemUser systemUser = systemUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), systemUser.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (systemUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account not verified. Please confirm OTP.");
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
        // Validate manager user exists
        SystemUser manager = systemUserRepository.findById(managerId)
                .orElseThrow(() -> new BadRequestException("Manager user not found"));

        if (manager.getUserType() != UserType.COMPANY_MANAGER) {
            throw new BadRequestException("Only company managers can register companies");
        }

        // Check if company name already exists
        if (wasteCompanyRepository.existsByName(request.getName())) {
            throw new BadRequestException("Company name already exists");
        }

        // Validate documents
        if (request.getRemaDocument() == null || request.getRemaDocument().isEmpty()) {
            throw new BadRequestException("REMA document is required");
        }
        if (request.getCityOfKigaliDocument() == null || request.getCityOfKigaliDocument().isEmpty()) {
            throw new BadRequestException("City of Kigali document is required");
        }
        if (request.getRdbDocument() == null || request.getRdbDocument().isEmpty()) {
            throw new BadRequestException("RDB document is required");
        }

        // Store documents
        String remaDocUrl = storeDocument(request.getRemaDocument(), "rema");
        String kigaliDocUrl = storeDocument(request.getCityOfKigaliDocument(), "kigali");
        String rdbDocUrl = storeDocument(request.getRdbDocument(), "rdb");

        // Create company
        WasteCompany company = WasteCompany.builder()
                .name(request.getName())
                .sectorCoverage(request.getSectorCoverage())
                .remaDocumentUrl(remaDocUrl)
                .cityOfKigaliDocumentUrl(kigaliDocUrl)
                .rdbDocumentUrl(rdbDocUrl)
                .createdBy(manager)
                .status(UserStatus.INACTIVE)
                .registrationStatus(RegistrationStatus.PENDING)
                .build();

        company = wasteCompanyRepository.save(company);

        // Create company user relationship
        CompanyUser companyUser = CompanyUser.builder()
                .systemUser(manager)
                .wasteCompany(company)
                .role(CompanyRole.MANAGER)
                .status(UserStatus.PENDING)
                .build();

        companyUserRepository.save(companyUser);

        // Notify all admins
        List<AdminUser> admins = adminUserRepository.findAll();
        for (AdminUser admin : admins) {
            Notification notification = Notification.builder()
                    .recipientUser(admin.getSystemUser())
                    .type(NotificationType.COMPANY_REGISTERED)
                    .title("New Company Registration")
                    .message("New company registration pending: " + request.getName())
                    .relatedEntityId(company.getId())
                    .build();
            notificationRepository.save(notification);
        }
    }

    private String storeDocument(org.springframework.web.multipart.MultipartFile file, String type) throws BadRequestException {
        try {
            // Upload to Cloudinary in the company-documents folder
            return cloudinaryService.uploadFile(file, "greenex/company-documents/" + type);
        } catch (Exception e) {
            throw new BadRequestException("Failed to store " + type + " document: " + e.getMessage());
        }
    }
}
