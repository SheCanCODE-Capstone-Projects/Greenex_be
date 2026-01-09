package org.example.greenexproject.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ComplaintResponse {

    private UUID id;
    private String type;
    private String description;
    private LocalDateTime createdAt;

    private UUID householdId;
    private UUID wasteCompanyId;

}
