package org.example.greenexproject.service;

import org.example.greenexproject.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // Get all notifications for a specific user
    List<NotificationResponse> getNotifications(UUID userId);

    // Delete a notification by its ID
    void deleteNotification(UUID notificationId);
}
