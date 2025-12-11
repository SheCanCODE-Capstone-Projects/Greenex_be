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
        Optional<CitizenAccount> existingAccount = citizenAccountRepository.findBySystemUser_Id(userId);
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

        // Generate unique household code
        String householdCode = generateHouseholdCode(zone);

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

        // Create citizen account linking user to household
        CitizenAccount citizenAccount = CitizenAccount.builder()
                .citizenUser(systemUser)
                .household(savedHousehold)
                .build();

        citizenAccountRepository.save(citizenAccount);

        return mapToResponse(savedHousehold, citizenAccount.getId());
    }

    private String generateHouseholdCode(Zone zone) {
        String prefix = zone.getSector().substring(0, Math.min(3, zone.getSector().length())).toUpperCase();
        long count = householdRepository.countByZone_Id(zone.getId());
        return String.format("%s-%05d", prefix, count + 1);
    }

    @Transactional(readOnly = true)
    public HouseholdResponse getMyHousehold(UUID userId) {
        CitizenAccount citizenAccount = citizenAccountRepository.findBySystemUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CitizenAccount", "userId", userId));

        return mapToResponse(citizenAccount.getHousehold(), citizenAccount.getId());
    }

    private HouseholdResponse mapToResponse(Household household, UUID citizenAccountId) {
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
                .createdAt(household.getCreatedAt())
                .build();
    }
}
