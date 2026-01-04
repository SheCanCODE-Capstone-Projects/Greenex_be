package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private String type; // convert enum to string
    private LocalDateTime createdAt;
}
