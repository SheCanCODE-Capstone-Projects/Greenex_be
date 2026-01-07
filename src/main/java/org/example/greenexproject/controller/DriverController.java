package org.example.greenexproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.response.SessionResponse;
import org.example.greenexproject.dto.response.StopResponse;
import org.example.greenexproject.model.enums.StopStatus;
import org.example.greenexproject.security.UserPrincipal;
import org.example.greenexproject.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @GetMapping("/sessions/today")
    public ResponseEntity<List<SessionResponse>> getTodaySessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SessionResponse> sessions = driverService.getTodaySessions(userPrincipal.getUserId());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<SessionResponse>> getUpcomingSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SessionResponse> sessions = driverService.getUpcomingSessions(userPrincipal.getUserId());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{id}/stops")
    public ResponseEntity<List<StopResponse>> getSessionStops(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        List<StopResponse> stops = driverService.getSessionStops(userPrincipal.getUserId(), id);
        return ResponseEntity.ok(stops);
    }

    @PatchMapping("/stops/{id}")
    public ResponseEntity<StopResponse> updateStopStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @RequestParam StopStatus status,
            @RequestParam(required = false) String reason) {
        StopResponse response = driverService.updateStopStatus(
                userPrincipal.getUserId(), id, status, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{id}/complete")
    public ResponseEntity<SessionResponse> completeSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        SessionResponse response = driverService.completeSession(userPrincipal.getUserId(), id);
        return ResponseEntity.ok(response);
    }
}
