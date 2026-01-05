package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateZoneRequest;
import org.example.greenexproject.dto.request.UpdateZoneRequest;
import org.example.greenexproject.dto.response.MessageResponse;
import org.example.greenexproject.dto.response.ZoneResponse;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.ZoneService;
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
@RequestMapping("/api/manager/zones")
@RequiredArgsConstructor
@Tag(name = "Manager - Zones", description = "Zone management endpoints for waste company managers")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('COMPANY_MANAGER')")
public class ZoneController {
    private final ZoneService zoneService;

    @PostMapping
    @Operation(summary = "Create zone", description = "Create a new zone (sector/cell/village) for the company")
    public ResponseEntity<ZoneResponse> createZone(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateZoneRequest request) {
        ZoneResponse response = zoneService.createZone(principal.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all zones", description = "Retrieve paginated list of zones for the company")
    public ResponseEntity<Page<ZoneResponse>> getZones(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ZoneResponse> zones = zoneService.getZonesByCompany(principal.getCompanyId(), pageable);
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID", description = "Retrieve a specific zone by its ID")
    public ResponseEntity<ZoneResponse> getZoneById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ZoneResponse zone = zoneService.getZoneById(id, principal.getCompanyId());
        return ResponseEntity.ok(zone);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update zone", description = "Update an existing zone")
    public ResponseEntity<ZoneResponse> updateZone(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateZoneRequest request) {
        ZoneResponse response = zoneService.updateZone(id, principal.getCompanyId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete zone", description = "Delete a zone (only if no households exist)")
    public ResponseEntity<MessageResponse> deleteZone(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        zoneService.deleteZone(id, principal.getCompanyId());
        return ResponseEntity.ok(new MessageResponse("Zone deleted successfully"));
    }

}