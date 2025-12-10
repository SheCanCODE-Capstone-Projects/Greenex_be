package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.BillingFrequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TariffPlanResponse {
    private UUID id;
    private String name;
    private String description;
    private BillingFrequency billingFrequency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private UUID companyId;
    private String companyName;
    private Long ruleCount;
    private LocalDateTime createdAt;
}
