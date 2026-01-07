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
public class DriverResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String licenseNumber;
    private String companyName;
    private UUID companyId;
    private String status;
    private LocalDateTime createdAt;
}
