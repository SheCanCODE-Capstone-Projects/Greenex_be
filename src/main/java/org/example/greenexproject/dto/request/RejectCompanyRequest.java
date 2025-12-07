package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectCompanyRequest {
//    @NotNull(message = "Company id is required")

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}