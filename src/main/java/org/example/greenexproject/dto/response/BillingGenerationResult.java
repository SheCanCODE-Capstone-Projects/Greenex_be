package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingGenerationResult {
    private Integer totalHouseholds;
    private Integer billsGenerated;
    private Integer billsSkipped;
    private String periodMonth;
    private String message;
}
