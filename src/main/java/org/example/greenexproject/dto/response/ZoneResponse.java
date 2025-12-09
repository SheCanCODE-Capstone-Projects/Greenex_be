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
public class ZoneResponse {
    private UUID id;
    private String sector;
    private String cell;
    private String village;
    private String code;
    private String description;
    private UUID companyId;
    private String companyName;
    private Long householdCount;
    private LocalDateTime createdAt;
}
