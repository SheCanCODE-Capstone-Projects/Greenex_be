package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.StopStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopResponse {
    private UUID id;

    private UUID householdId;

    private String householdAddress;

    private String citizenName;

    private Integer sequenceNumber;

    private LocalTime plannedTime;

    private StopStatus status;

    private String reason;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
}
