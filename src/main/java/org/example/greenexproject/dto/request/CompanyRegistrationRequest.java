package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CompanyRegistrationRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    @NotBlank(message = "Sector coverage is required")
    private String sectorCoverage;

    @NotNull(message = "REMA document is required")
    private MultipartFile remaDocument;

    @NotNull(message = "City of Kigali document is required")
    private MultipartFile cityOfKigaliDocument;

    @NotNull(message = "RDB document is required")
    private MultipartFile rdbDocument;
}
