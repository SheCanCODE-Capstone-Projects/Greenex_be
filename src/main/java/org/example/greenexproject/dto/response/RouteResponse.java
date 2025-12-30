package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.DayOfWeek;
import org.example.greenexproject.model.enums.RouteStatus;
import org.example.greenexproject.model.enums.Shift;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {
    private UUID id;

    private UUID zoneId;

    private String zoneName;

    private String name;

    private DayOfWeek dayOfWeek;

    private Shift shift;

    private RouteStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
