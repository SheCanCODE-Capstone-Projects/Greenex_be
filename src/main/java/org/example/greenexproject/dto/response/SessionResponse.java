package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private UUID id;

    private UUID routeId;

    private String routeName;

    private String zoneName;

    private UUID driverUserId;

    private String driverName;

    private LocalDate date;

    private SessionStatus status;

    private Integer totalStops;

    private Integer completedStops;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
