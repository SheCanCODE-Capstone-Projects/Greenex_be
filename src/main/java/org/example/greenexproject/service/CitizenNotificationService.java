package org.example.greenexproject.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.response.CitizenNotificationResponse;
import org.example.greenexproject.dto.response.NotificationCountResponse;
import org.example.greenexproject.model.entity.Notification;
import org.example.greenexproject.model.enums.NotificationType;
import org.example.greenexproject.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenNotificationService {

    private final NotificationRepository notificationRepository;


    // Get all notifications for the authenticated citizen
    @Transactional(readOnly = true)
    public List<CitizenNotificationResponse> getAllNotifications(UUID citizenId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientUser_IdOrderByCreatedAtDesc(citizenId, pageable);

        return notificationPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get paginated notifications
    @Transactional(readOnly = true)
    public Page<CitizenNotificationResponse> getPaginatedNotifications(
            UUID citizenId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientUser_IdOrderByCreatedAtDesc(citizenId, pageable);

        return notificationPage.map(this::mapToResponse);
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public List<CitizenNotificationResponse> getUnreadNotifications(UUID citizenId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientUser_IdAndReadAtIsNullOrderByCreatedAtDesc(citizenId);

        return notifications.stream()
                .map(this::mapToResponse)
                .toList();
    }

//    // Get notifications by type
//    @Transactional(readOnly = true)
//    public List<CitizenNotificationResponse> getNotificationsByType(
//            UUID citizenId,
//            NotificationType type) {
//
//        // First get all notifications, then filter by type
//        Pageable pageable = PageRequest.of(0, 100, Sort.by("createdAt").descending());
//        Page<Notification> notificationPage = notificationRepository
//                .findByRecipientUser_IdOrderByCreatedAtDesc(citizenId, pageable);
//
//        return notificationPage.getContent().stream()
//                .filter(notification -> notification.getType() == type)
//                .map(this::mapToResponse)
//                .toList();
//    }


    // Mark single notification as read
    @Transactional
    public CitizenNotificationResponse markAsRead(UUID notificationId, UUID citizenId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // Security check: ensure citizen owns this notification
        if (!notification.getRecipientUser().getId().equals(citizenId)) {
            throw new SecurityException("Not authorized to modify this notification");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification {} marked as read by citizen {}", notificationId, citizenId);
        }

        return mapToResponse(notification);
    }

    // Mark multiple notifications as read
    @Transactional
    public int markMultipleAsRead(List<UUID> notificationIds, UUID citizenId) {
        int updatedCount = 0;

        for (UUID notificationId : notificationIds) {
            try {
                Notification notification = notificationRepository.findById(notificationId)
                        .orElse(null);

                if (notification != null &&
                        notification.getRecipientUser().getId().equals(citizenId) &&
                        notification.getReadAt() == null) {

                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    updatedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to mark notification {} as read: {}", notificationId, e.getMessage());
            }
        }

        log.info("{} notifications marked as read by citizen {}", updatedCount, citizenId);
        return updatedCount;
    }

    // Mark all notifications as read
    @Transactional
    public int markAllAsRead(UUID citizenId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientUser_IdAndReadAtIsNullOrderByCreatedAtDesc(citizenId);

        unreadNotifications.forEach(notification ->
                notification.setReadAt(LocalDateTime.now())
        );

        notificationRepository.saveAll(unreadNotifications);

        int updatedCount = unreadNotifications.size();
        log.info("All notifications marked as read by citizen {}, count: {}", citizenId, updatedCount);
        return updatedCount;
    }

    // Delete a notification
    @Transactional
    public void deleteNotification(UUID notificationId, UUID citizenId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // Security check
        if (!notification.getRecipientUser().getId().equals(citizenId)) {
            throw new SecurityException("Not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by citizen {}", notificationId, citizenId);
    }

    // Delete all notifications for citizen
    @Transactional
    public void deleteAllNotifications(UUID citizenId) {
        // Get all notifications for citizen and delete them
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientUser_IdOrderByCreatedAtDesc(citizenId, pageable);

        List<Notification> notifications = notificationPage.getContent();
        notificationRepository.deleteAll(notifications);

        log.info("All notifications deleted for citizen {}, count: {}", citizenId, notifications.size());
    }

    // Helper method to map entity to response
    private CitizenNotificationResponse mapToResponse(Notification notification) {
        // Derive title from notification type
        String title = getTitleFromType(notification.getType());

        // Calculate time ago for UI
        String timeAgo = calculateTimeAgo(notification.getCreatedAt());

        return CitizenNotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .metadata(notification.getMetadata())
                .isRead(notification.getReadAt() != null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .title(title)
                .timeAgo(timeAgo)
                .build();
    }

    // Helper method to get title from notification type
    private String getTitleFromType(NotificationType type) {
        if (type == null) return "Notification";

        switch (type) {
            case PAYMENT:
                return "Payment Notification";
            case COMPLAINT:
                return "Complaint Update";
          //  case SERVICE:
               // return "Service Alert";
           case BILLING:
                return "Billing Information";
            //case SYSTEM:
              //  return "System Notification";
            case PROMOTIONAL:
                return "Promotional Message";
            default:
                return "Notification";
        }
    }

    // Helper method to calculate "time ago" for UI display
    private String calculateTimeAgo(LocalDateTime createdTime) {
        if (createdTime == null) return "";

        Duration duration = Duration.between(createdTime, LocalDateTime.now());

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toHours() < 1) {
            long minutes = duration.toMinutes();
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (duration.toDays() < 1) {
            long hours = duration.toHours();
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (duration.toDays() < 30) {
            long days = duration.toDays();
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            return "Over a month ago";
        }
    }
}