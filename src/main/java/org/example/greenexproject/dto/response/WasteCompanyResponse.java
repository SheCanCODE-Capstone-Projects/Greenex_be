package org.example.greenexproject.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.RegistrationStatus;
import org.example.greenexproject.model.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteCompanyResponse {
    private UUID id;
    private String name;
    private String contractNumber;
    private String sectorCoverage;
    private UserStatus status;
    private RegistrationStatus registrationStatus;
    private String createdByName;
    private String createdByEmail;
    private String remaDocumentUrl;
    private String cityOfKigaliDocumentUrl;
    private String rdbDocumentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}