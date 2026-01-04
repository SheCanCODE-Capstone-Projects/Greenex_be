package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.NotificationRequest;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.example.greenexproject.model.entity.Notification;
import org.example.greenexproject.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private NotificationResponse mapToDTO(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .message(request.getMessage())
                .type(request.getType())
                // optionally store related entity ID in metadata as JSON
                .metadata("{\"complaintId\":\"" + request.getComplaintId() + "\"}")
                // You could also link the company/manager if you have recipientUser
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    @Override
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }
}
