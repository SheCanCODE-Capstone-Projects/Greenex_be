package org.example.greenexproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.greenexproject.model.enums.HouseType;
import org.example.greenexproject.model.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdResponse {
    private UUID id;
    private String code;
    private String address;
    private HouseType houseType;
    private UserStatus status;
    private String notes;

    // Zone info
    private UUID zoneId;
    private String zoneSector;
    private String zoneCell;
    private String zoneVillage;

    // Company info
    private UUID companyId;
    private String companyName;

    // Citizen info (if linked)
    private UUID citizenAccountId;
    private UUID citizenUserId;
    private String citizenName;
    private String citizenPhone;
    private String citizenEmail;

    private LocalDateTime createdAt;
}
