package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.GenerateBillingRequest;
import org.example.greenexproject.dto.response.BillingGenerationResult;
import org.example.greenexproject.dto.response.PaymentResponse;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.BillingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/billing")
@RequiredArgsConstructor
@Tag(name = "Manager - Billing", description = "Billing management endpoints for waste company managers")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('COMPANY_MANAGER')")
public class BillingController {
    private final BillingService billingService;

    @PostMapping("/generate")
    @Operation(summary = "Generate monthly bills", description = "Generate bills for all active households for a specific month")
    public ResponseEntity<BillingGenerationResult> generateMonthlyBills(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GenerateBillingRequest request) {
        BillingGenerationResult result = billingService.generateMonthlyBills(
                principal.getCompanyId(), request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments", description = "Retrieve paginated list of all payments")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = billingService.getPaymentsByCompany(
                principal.getCompanyId(), pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/period/{periodMonth}")
    @Operation(summary = "Get payments by period", description = "Retrieve payments for a specific month (format: YYYY-MM)")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByPeriod(
            @PathVariable String periodMonth,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = billingService.getPaymentsByPeriod(
                principal.getCompanyId(), periodMonth, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/unpaid")
    @Operation(summary = "Get unpaid payments", description = "Retrieve all pending/unpaid payments")
    public ResponseEntity<Page<PaymentResponse>> getUnpaidPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = billingService.getUnpaidPayments(
                principal.getCompanyId(), pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/overdue")
    @Operation(summary = "Get overdue payments", description = "Retrieve all overdue payments")
    public ResponseEntity<Page<PaymentResponse>> getOverduePayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = billingService.getOverduePayments(
                principal.getCompanyId(), pageable);
        return ResponseEntity.ok(payments);
    }
}
