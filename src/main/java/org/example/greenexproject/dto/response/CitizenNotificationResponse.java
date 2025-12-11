package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenNotificationResponse {
    private UUID id;
    private String message;  // From your entity
    private NotificationType type;  // From your entity
    private String metadata;  // From your entity
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Title can be derived from type if needed
    private String title;

    // Time since creation (for UI display)
    private String timeAgo;
}