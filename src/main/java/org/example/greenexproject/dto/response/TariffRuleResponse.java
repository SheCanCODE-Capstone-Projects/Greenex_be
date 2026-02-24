package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.HouseType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TariffRuleResponse {
    private UUID id;
    private UUID tariffPlanId;
    private String tariffPlanName;

    // Zone info (if applicable)
    private UUID zoneId;
    private String zoneSector;
    private String zoneCell;
    private String zoneVillage;

    // House type (if applicable)
    private HouseType houseType;

    private Integer pickupFrequencyPerWeek;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
