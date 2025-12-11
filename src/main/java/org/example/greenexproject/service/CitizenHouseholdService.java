package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateHouseholdRequest;
import org.example.greenexproject.dto.response.HouseholdResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.CitizenAccount;
import org.example.greenexproject.model.entity.Household;
import org.example.greenexproject.model.entity.SystemUser;
import org.example.greenexproject.model.entity.Zone;
import org.example.greenexproject.repository.CitizenAccountRepository;
import org.example.greenexproject.repository.HouseholdRepository;
import org.example.greenexproject.repository.SystemUserRepository;
import org.example.greenexproject.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitizenHouseholdService {
    private final HouseholdRepository householdRepository;
    private final ZoneRepository zoneRepository;
    private final SystemUserRepository systemUserRepository;
    private final CitizenAccountRepository citizenAccountRepository;

    @Transactional
    public HouseholdResponse createAndLinkHousehold(UUID userId, CreateHouseholdRequest request) {
        SystemUser systemUser = systemUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user already has a household
        Optional<CitizenAccount> existingAccount = citizenAccountRepository.findByCitizenUser_Id(userId);
        if (existingAccount.isPresent()) {
            throw new BadRequestException("You already have a household registered");
        }

        // Automatically find zone based on sector, cell, and village
        Zone zone = zoneRepository.findBySectorAndCellAndVillage(
                        request.getSector(),
                        request.getCell(),
                        request.getVillage())
                .orElseThrow(() -> new BadRequestException(
                        "No waste management service available in your area: " +
                                request.getVillage() + ", " + request.getCell() + ", " + request.getSector()));

        // Validate zone has a waste company
        if (zone.getWasteCompany() == null) {
            throw new BadRequestException("No waste management company assigned to your area");
        }

        // Generate unique household code (with race condition protection)
        String householdCode = generateUniqueHouseholdCode(zone);

        // Create household
        Household household = Household.builder()
                .zone(zone)
                .wasteCompany(zone.getWasteCompany())
                .code(householdCode)
                .address(request.getAddress())
                .houseType(request.getHouseType())
                .notes(request.getNotes())
                .build();

        Household savedHousehold = householdRepository.save(household);
        householdRepository.flush(); // Ensure @CreationTimestamp is populated

        // Create citizen account linking user to household
        CitizenAccount citizenAccount = CitizenAccount.builder()
                .citizenUser(systemUser)
                .household(savedHousehold)
                .build();

        CitizenAccount savedCitizenAccount = citizenAccountRepository.save(citizenAccount);

        return mapToResponse(savedHousehold, savedCitizenAccount);
    }

    private String generateUniqueHouseholdCode(Zone zone) {
        String prefix = zone.getSector().substring(0, Math.min(3, zone.getSector().length())).toUpperCase();
        String code;
        int attempts = 0;
        do {
            long count = householdRepository.countByZone_Id(zone.getId());
            code = String.format("%s-%05d", prefix, count + 1 + attempts);
            attempts++;
        } while (householdRepository.existsByCode(code) && attempts < 100);

        if (householdRepository.existsByCode(code)) {
            throw new BadRequestException("Failed to generate unique household code");
        }
        return code;
    }

    @Transactional(readOnly = true)
    public HouseholdResponse getMyHousehold(UUID userId) {
        CitizenAccount citizenAccount = citizenAccountRepository.findByCitizenUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CitizenAccount", "userId", userId));

        return mapToResponse(citizenAccount.getHousehold(), citizenAccount);
    }

    private HouseholdResponse mapToResponse(Household household, UUID citizenAccountId) {
        CitizenAccount citizenAccount = household.getCitizenAccount();

        return HouseholdResponse.builder()
                .id(household.getId())
                .code(household.getCode())
                .address(household.getAddress())
                .houseType(household.getHouseType())
                .status(household.getStatus())
                .notes(household.getNotes())
                .zoneId(household.getZone().getId())
                .zoneSector(household.getZone().getSector())
                .zoneCell(household.getZone().getCell())
                .zoneVillage(household.getZone().getVillage())
                .companyId(household.getWasteCompany().getId())
                .companyName(household.getWasteCompany().getName())
                .citizenAccountId(citizenAccountId)
                .citizenUserId(citizenAccount != null ? citizenAccount.getCitizenUser().getId() : null)
                .citizenName(citizenAccount != null ? citizenAccount.getCitizenUser().getFullName() : null)
                .citizenPhone(citizenAccount != null ? citizenAccount.getCitizenUser().getPhone() : null)
                .citizenEmail(citizenAccount != null ? citizenAccount.getCitizenUser().getEmail() : null)
                .createdAt(household.getCreatedAt())
                .build();
    }

    private HouseholdResponse mapToResponse(Household household, CitizenAccount citizenAccount) {
        return HouseholdResponse.builder()
                .id(household.getId())
                .code(household.getCode())
                .address(household.getAddress())
                .houseType(household.getHouseType())
                .status(household.getStatus())
                .notes(household.getNotes())
                .zoneId(household.getZone().getId())
                .zoneSector(household.getZone().getSector())
                .zoneCell(household.getZone().getCell())
                .zoneVillage(household.getZone().getVillage())
                .companyId(household.getWasteCompany().getId())
                .companyName(household.getWasteCompany().getName())
                .citizenAccountId(citizenAccount.getId())
                .citizenUserId(citizenAccount.getCitizenUser().getId())
                .citizenName(citizenAccount.getCitizenUser().getFullName())
                .citizenPhone(citizenAccount.getCitizenUser().getPhone())
                .citizenEmail(citizenAccount.getCitizenUser().getEmail())
                .createdAt(household.getCreatedAt())
                .build();
    }
}
