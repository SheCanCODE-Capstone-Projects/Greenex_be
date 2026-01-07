package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateDriverRequest;
import org.example.greenexproject.dto.response.DriverResponse;
import org.example.greenexproject.dto.response.MessageResponse;
import org.example.greenexproject.model.enums.UserStatus;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.DriverManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/manager/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver Management", description = "Company Manager manages driver accounts")
@PreAuthorize("hasRole('COMPANY_MANAGER')")
public class DriverManagementController {
    private final DriverManagementService driverManagementService;

    @PostMapping
    @Operation(summary = "Create driver account", description = "Company manager creates a driver account for their company")
    public ResponseEntity<DriverResponse> createDriver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = driverManagementService.createDriver(currentUser.getUserId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get company drivers", description = "Get all drivers for the manager's company")
    public ResponseEntity<Page<DriverResponse>> getCompanyDrivers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {
        Page<DriverResponse> drivers = driverManagementService.getCompanyDrivers(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver details", description = "Get details of a specific driver")
    public ResponseEntity<DriverResponse> getDriver(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID driverId) {
        DriverResponse driver = driverManagementService.getDriver(currentUser.getUserId(), driverId);
        return ResponseEntity.ok(driver);
    }

    @PatchMapping("/{driverId}/status")
    @Operation(summary = "Update driver status", description = "Activate or deactivate a driver")
    public ResponseEntity<MessageResponse> updateDriverStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID driverId,
            @RequestParam UserStatus status) {
        driverManagementService.updateDriverStatus(currentUser.getUserId(), driverId, status);
        return ResponseEntity.ok(new MessageResponse("Driver status updated successfully"));
    }
}
