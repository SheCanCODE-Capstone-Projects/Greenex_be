package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateTariffPlanRequest;
import org.example.greenexproject.dto.request.CreateTariffRuleRequest;
import org.example.greenexproject.dto.response.MessageResponse;
import org.example.greenexproject.dto.response.TariffPlanResponse;
import org.example.greenexproject.dto.response.TariffRuleResponse;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.TariffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/manager/tariffs")
@RequiredArgsConstructor
@Tag(name = "Manager - Tariffs", description = "Tariff management endpoints for waste company managers")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('COMPANY_MANAGER')")
public class TariffController {
    private final TariffService tariffService;

    @PostMapping("/plans")
    @Operation(summary = "Create tariff plan", description = "Create a new tariff plan for the company")
    public ResponseEntity<TariffPlanResponse> createTariffPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTariffPlanRequest request) {
        TariffPlanResponse response = tariffService.createTariffPlan(principal.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/plans")
    @Operation(summary = "Get all tariff plans", description = "Retrieve paginated list of all tariff plans")
    public ResponseEntity<Page<TariffPlanResponse>> getTariffPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TariffPlanResponse> plans = tariffService.getTariffPlansByCompany(
                principal.getCompanyId(), pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/active")
    @Operation(summary = "Get active tariff plans", description = "Retrieve currently active tariff plans")
    public ResponseEntity<Page<TariffPlanResponse>> getActiveTariffPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TariffPlanResponse> plans = tariffService.getActiveTariffPlans(
                principal.getCompanyId(), pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{id}")
    @Operation(summary = "Get tariff plan by ID", description = "Retrieve a specific tariff plan")
    public ResponseEntity<TariffPlanResponse> getTariffPlanById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        TariffPlanResponse plan = tariffService.getTariffPlanById(id, principal.getCompanyId());
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/rules")
    @Operation(summary = "Create tariff rule", description = "Create a tariff rule (pricing for zone/house type)")
    public ResponseEntity<TariffRuleResponse> createTariffRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTariffRuleRequest request) {
        TariffRuleResponse response = tariffService.createTariffRule(principal.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/plans/{planId}/rules")
    @Operation(summary = "Get rules by plan", description = "Retrieve all tariff rules for a specific plan")
    public ResponseEntity<Page<TariffRuleResponse>> getTariffRulesByPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TariffRuleResponse> rules = tariffService.getTariffRulesByPlan(
                planId, principal.getCompanyId(), pageable);
        return ResponseEntity.ok(rules);
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete tariff rule", description = "Delete a tariff rule")
    public ResponseEntity<MessageResponse> deleteTariffRule(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        tariffService.deleteTariffRule(id, principal.getCompanyId());
        return ResponseEntity.ok(new MessageResponse("Tariff rule deleted successfully"));
    }
}
