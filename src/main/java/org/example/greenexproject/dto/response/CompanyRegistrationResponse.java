package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegistrationResponse {
    private UUID companyId;
    private String companyName;
    private String registrationNumber;
    private String status; // PENDING, APPROVED, REJECTED
    private String message;
    private LocalDateTime registeredAt;
}