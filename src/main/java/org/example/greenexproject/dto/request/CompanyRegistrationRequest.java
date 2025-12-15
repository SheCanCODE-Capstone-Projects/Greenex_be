package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    private String contractNumber;

    @NotBlank(message = "Sector coverage is required")
    private String sectorCoverage;
}
