package org.example.greenexproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectCompanyRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}