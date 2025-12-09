package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.HouseType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTariffRuleRequest {
    @NotNull(message = "Tariff plan ID is required")
    private UUID tariffPlanId;

    private UUID zoneId;  // Optional: applies to specific zone

    private HouseType houseType;  // Optional: applies to specific house type

    @NotNull(message = "Pickup frequency per week is required")
    private Integer pickupFrequencyPerWeek;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
}
