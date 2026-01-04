package org.example.greenexproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.NotificationType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    // Notification content shown to manager
    private String message;

    // Type of notification (NEW_COMPLAINT, PAYMENT_RECEIVED, etc.)
    private NotificationType type;

    // Company that should receive this notification
    private UUID companyId;

    // Related entity (optional but very useful)
    private UUID complaintId;
}
