package org.example.greenexproject.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.RejectCompanyRequest;
import org.example.greenexproject.dto.response.MessageResponse;
import org.example.greenexproject.dto.response.WasteCompanyResponse;
import org.example.greenexproject.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_MANAGER')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/companies/pending")
    @Operation(summary = "Get pending companies",
            description = "Retrieve all companies with PENDING registration status")
    public ResponseEntity<Page<WasteCompanyResponse>> getPendingCompanies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WasteCompanyResponse> companies = adminService.getPendingCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/companies/approved")
    @Operation(summary = "Get approved companies",
            description = "Retrieve all companies with APPROVED registration status")
    public ResponseEntity<Page<WasteCompanyResponse>> getApprovedCompanies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<WasteCompanyResponse> companies = adminService.getApprovedCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping("/companies/{companyId}/approve")
    @Operation(summary = "Approve company",
            description = "Approve a pending company registration")
    public ResponseEntity<MessageResponse> approveCompany(@PathVariable UUID companyId) throws BadRequestException {
        adminService.approveCompany(companyId);
        return ResponseEntity.ok(new MessageResponse("Company approved successfully"));
    }

    @PostMapping("/companies/{companyId}/reject")
    @Operation(summary = "Reject company",
            description = "Reject a pending company registration with a Reason")
    public ResponseEntity<MessageResponse> rejectCompany(
            @PathVariable UUID companyId,
            @Valid
            @RequestBody RejectCompanyRequest request) throws BadRequestException {
        adminService.rejectCompany(companyId, request);
        return ResponseEntity.ok(new MessageResponse("Company rejected successfully"));
    }
}