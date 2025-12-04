package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.CompanyRegistrationRequest;
import org.example.greenexproject.dto.request.LoginRequest;
import org.example.greenexproject.dto.request.RegisterRequest;
import org.example.greenexproject.dto.response.AuthResponse;
import org.example.greenexproject.dto.response.MessageResponse;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {
    private final AuthService authService;

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

    @PostMapping("/company/register")
    @Operation(summary = "Register company", description = "Manager registers their waste company")
    public ResponseEntity<MessageResponse> registerCompany(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CompanyRegistrationRequest request) throws BadRequestException {
        authService.registerCompany(currentUser.getUserId(), request);
        return ResponseEntity.ok(new MessageResponse(
                "Company registration submitted successfully. Awaiting admin approval."));
    }
}
