package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePickupSessionRequest {
    @NotNull(message = "Route ID is required")
    private UUID routeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private UUID driverUserId;
}
