package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.LoginRequest;
import org.example.greenexproject.dto.request.OtpVerificationRequest;
import org.example.greenexproject.dto.request.RegisterRequest;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.dto.response.MessageResponse;

import org.example.greenexproject.dto.request.CompanyRegistrationRequest;

import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.AuthService;
import org.example.greenexproject.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register as Citizen, Manager, or Admin")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) throws BadRequestException {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/company/register", consumes = "multipart/form-data")
    @Operation(summary = "Register company", description = "Manager registers their waste company")
    public ResponseEntity<MessageResponse> registerCompany(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @ModelAttribute CompanyRegistrationRequest request) throws BadRequestException {
        authService.registerCompany(currentUser.getUserId(), request);
        return ResponseEntity.ok(new MessageResponse(
                "Company registration submitted successfully. Awaiting admin approval."));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify user account using OTP")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        boolean verified = authService.verifyOtp(request.getOtp());
        if (verified) {
            return ResponseEntity.ok(new MessageResponse("OTP verified successfully. You can now log in."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid or expired OTP."));
        }
    }

    public OtpService getOtpService() {
        return otpService;
    }
}
