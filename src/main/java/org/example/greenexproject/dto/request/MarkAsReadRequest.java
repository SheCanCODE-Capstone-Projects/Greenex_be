// MarkAsReadRequest.java
package org.example.greenexproject.dto.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class MarkAsReadRequest {
    private List<UUID> notificationIds; // For bulk operations
    private boolean markAll; // Flag to mark all as read
}