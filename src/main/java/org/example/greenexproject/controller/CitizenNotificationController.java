package org.example.greenexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.response.CitizenNotificationResponse;
import org.example.greenexproject.service.CitizenNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/citizen/notifications")
@RequiredArgsConstructor
@Tag(name = "Citizen Notifications", description = "APIs for citizens to manage their notifications")
@Slf4j
public class CitizenNotificationController {

    private final CitizenNotificationService notificationService;

    @Operation(summary = "Get all notifications",
            description = "Retrieve all notifications for the authenticated citizen")
    @GetMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<CitizenNotificationResponse>> getAllNotifications(
            @Parameter(description = "Filter by notification type")
            @RequestParam(required = false) org.example.greenexproject.model.enums.NotificationType type) {

        UUID citizenId = getCurrentUserId();
        log.info("Citizen {} fetching notifications", citizenId);

        List<CitizenNotificationResponse> notifications = notificationService.getAllNotifications(citizenId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications",
            description = "Retrieve only unread notifications")
    @GetMapping("/unread")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<CitizenNotificationResponse>> getUnreadNotifications() {
        UUID citizenId = getCurrentUserId();
        log.info("Citizen {} fetching unread notifications", citizenId);

        List<CitizenNotificationResponse> notifications =
                notificationService.getUnreadNotifications(citizenId);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Delete notification",
            description = "Delete a specific notification")
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @Parameter(description = "ID of the notification to delete")
            @PathVariable UUID notificationId) {

        UUID citizenId = getCurrentUserId();
        log.info("Citizen {} deleting notification {}", citizenId, notificationId);

        notificationService.deleteNotification(notificationId, citizenId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete all notifications",
            description = "Delete all notifications for the citizen")
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Map<String, String>> deleteAllNotifications() {
        UUID citizenId = getCurrentUserId();
        log.info("Citizen {} deleting all notifications", citizenId);

        notificationService.deleteAllNotifications(citizenId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications deleted successfully");

        return ResponseEntity.ok(response);
    }

    // Helper method to get current user ID
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Get the username from authentication
            String username = authentication.getName();

            log.warn("Using placeholder user ID. Implement getCurrentUserId() properly!");
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        throw new RuntimeException("User not authenticated");
    }
}