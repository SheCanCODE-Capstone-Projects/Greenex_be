package org.example.greenexproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateHouseholdRequest;
import org.example.greenexproject.dto.response.HouseholdResponse;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.CitizenHouseholdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citizen/household")
@RequiredArgsConstructor
public class CitizenHouseholdController {
    private final CitizenHouseholdService householdService;

    @PostMapping
    public ResponseEntity<HouseholdResponse> createHousehold(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateHouseholdRequest request) {
        HouseholdResponse response = householdService.createAndLinkHousehold(
                userPrincipal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<HouseholdResponse> getMyHousehold(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        HouseholdResponse response = householdService.getMyHousehold(userPrincipal.getUserId());
        return ResponseEntity.ok(response);
    }
}
