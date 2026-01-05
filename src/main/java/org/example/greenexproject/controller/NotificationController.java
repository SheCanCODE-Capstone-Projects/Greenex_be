package org.example.greenexproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.example.greenexproject.service.NotificationService;
import org.example.greenexproject.security.UserPrincipal; // your security principal class
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // GET all notifications for the logged-in user (company manager)
    @GetMapping
    public List<NotificationResponse> getAllNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID currentUserId = userPrincipal.getUserId(); // <-- adjust getter to match your UserPrincipal
        return notificationService.getNotifications(currentUserId);
    }

    // DELETE a notification by its ID
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
    }
}
