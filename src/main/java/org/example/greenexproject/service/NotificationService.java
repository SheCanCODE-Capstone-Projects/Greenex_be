package org.example.greenexproject.service;

import org.example.greenexproject.dto.request.NotificationRequest;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponse> getAllNotifications(Pageable pageable);

    NotificationResponse createNotification(NotificationRequest request);

    void deleteNotification(UUID id);
}
