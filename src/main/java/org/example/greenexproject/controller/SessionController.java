package org.example.greenexproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreatePickupSessionRequest;
import org.example.greenexproject.dto.response.SessionResponse;
import org.example.greenexproject.dto.response.StopResponse;
import org.example.greenexproject.model.enums.SessionStatus;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreatePickupSessionRequest request) {
        SessionResponse response = sessionService.createSession(userPrincipal.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SessionResponse>> listSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        Page<SessionResponse> sessions = sessionService.listSessions(userPrincipal.getCompanyId(), pageable);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        SessionResponse response = sessionService.getSessionById(userPrincipal.getCompanyId(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopResponse>> getSessionStops(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        List<StopResponse> stops = sessionService.getSessionStops(userPrincipal.getCompanyId(), id);
        return ResponseEntity.ok(stops);
    }

    @PatchMapping("/{id}/driver/{driverId}")
    public ResponseEntity<SessionResponse> assignDriver(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @PathVariable UUID driverId) {
        SessionResponse response = sessionService.assignDriver(userPrincipal.getCompanyId(), id, driverId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SessionResponse> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @RequestParam SessionStatus status) {
        SessionResponse response = sessionService.updateSessionStatus(userPrincipal.getCompanyId(), id, status);
        return ResponseEntity.ok(response);
    }
}
