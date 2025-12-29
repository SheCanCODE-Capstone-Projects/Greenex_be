package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.DayOfWeek;
import org.example.greenexproject.model.enums.Shift;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {
    @NotNull(message = "Zone ID is required")
    private UUID zoneId;

    @NotBlank(message = "Route name is required")
    private String name;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Shift is required")
    private Shift shift;
}
