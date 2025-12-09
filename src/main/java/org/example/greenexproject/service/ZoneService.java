package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateZoneRequest;
import org.example.greenexproject.dto.request.UpdateZoneRequest;
import org.example.greenexproject.dto.response.ZoneResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.entity.Zone;
import org.example.greenexproject.repository.HouseholdRepository;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.example.greenexproject.repository.ZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final WasteCompanyRepository wasteCompanyRepository;
    private final HouseholdRepository householdRepository;

    @Transactional
    public ZoneResponse createZone(UUID companyId, CreateZoneRequest request) {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Check if zone already exists for this company
        if (zoneRepository.findBySectorAndCellAndVillage(
                request.getSector(), request.getCell(), request.getVillage()).isPresent()) {
            throw new BadRequestException("Zone already exists with these sector, cell, and village");
        }

        Zone zone = new Zone();
        zone.setSector(request.getSector());
        zone.setCell(request.getCell());
        zone.setVillage(request.getVillage());
        zone.setCode(request.getCode());
        zone.setDescription(request.getDescription());
        zone.setWasteCompany(company);

        Zone savedZone = zoneRepository.save(zone);
        return mapToResponse(savedZone);
    }

    @Transactional(readOnly = true)
    public Page<ZoneResponse> getZonesByCompany(UUID companyId, Pageable pageable) {
        Page<Zone> zones = zoneRepository.findByWasteCompany_Id(companyId, pageable);
        return zones.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ZoneResponse getZoneById(UUID zoneId, UUID companyId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));

        // Ensure zone belongs to the company
        if (!zone.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        return mapToResponse(zone);
    }

    @Transactional
    public ZoneResponse updateZone(UUID zoneId, UUID companyId, UpdateZoneRequest request) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));

        // Ensure zone belongs to the company
        if (!zone.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        if (request.getSector() != null) {
            zone.setSector(request.getSector());
        }
        if (request.getCell() != null) {
            zone.setCell(request.getCell());
        }
        if (request.getVillage() != null) {
            zone.setVillage(request.getVillage());
        }
        if (request.getCode() != null) {
            zone.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            zone.setDescription(request.getDescription());
        }

        Zone updatedZone = zoneRepository.save(zone);
        return mapToResponse(updatedZone);
    }

    @Transactional
    public void deleteZone(UUID zoneId, UUID companyId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));

        // Ensure zone belongs to the company
        if (!zone.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Zone", "id", zoneId);
        }

        // Check if zone has households
        long householdCount = householdRepository.countByZone_Id(zoneId);
        if (householdCount > 0) {
            throw new BadRequestException("Cannot delete zone with " + householdCount + " households. Reassign or delete households first.");
        }

        zoneRepository.delete(zone);
    }

    private ZoneResponse mapToResponse(Zone zone) {
        long householdCount = householdRepository.countByZone_Id(zone.getId());

        return ZoneResponse.builder()
                .id(zone.getId())
                .sector(zone.getSector())
                .cell(zone.getCell())
                .village(zone.getVillage())
                .code(zone.getCode())
                .description(zone.getDescription())
                .companyId(zone.getWasteCompany().getId())
                .companyName(zone.getWasteCompany().getName())
                .householdCount(householdCount)
                .createdAt(zone.getCreatedAt())
                .build();
    }
}
