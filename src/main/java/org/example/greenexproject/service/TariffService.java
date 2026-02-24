package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateTariffPlanRequest;
import org.example.greenexproject.dto.request.CreateTariffRuleRequest;
import org.example.greenexproject.dto.response.TariffPlanResponse;
import org.example.greenexproject.dto.response.TariffRuleResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.TariffPlan;
import org.example.greenexproject.model.entity.TariffRule;
import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.entity.Zone;
import org.example.greenexproject.repository.TariffPlanRepository;
import org.example.greenexproject.repository.TariffRuleRepository;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.example.greenexproject.repository.ZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffService {
    private final TariffPlanRepository tariffPlanRepository;
    private final TariffRuleRepository tariffRuleRepository;
    private final WasteCompanyRepository wasteCompanyRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public TariffPlanResponse createTariffPlan(UUID companyId, CreateTariffPlanRequest request) {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Validate dates
        if (request.getEffectiveTo() != null &&
                request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BadRequestException("Effective to date must be after effective from date");
        }

        TariffPlan tariffPlan = new TariffPlan();
        tariffPlan.setName(request.getName());
        tariffPlan.setDescription(request.getDescription());
        tariffPlan.setBillingFrequency(request.getBillingFrequency());
        tariffPlan.setEffectiveFrom(request.getEffectiveFrom());
        tariffPlan.setEffectiveTo(request.getEffectiveTo());
        tariffPlan.setWasteCompany(company);

        TariffPlan savedPlan = tariffPlanRepository.save(tariffPlan);
        return mapPlanToResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public Page<TariffPlanResponse> getTariffPlansByCompany(UUID companyId, Pageable pageable) {
        Page<TariffPlan> plans = tariffPlanRepository.findByWasteCompany_Id(companyId, pageable);
        return plans.map(this::mapPlanToResponse);
    }

    @Transactional(readOnly = true)
    public Page<TariffPlanResponse> getActiveTariffPlans(UUID companyId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<TariffPlan> plans = tariffPlanRepository.findByWasteCompany_IdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                companyId, today, today, pageable);
        return plans.map(this::mapPlanToResponse);
    }

    @Transactional(readOnly = true)
    public TariffPlanResponse getTariffPlanById(UUID planId, UUID companyId) {
        TariffPlan plan = tariffPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "id", planId));

        if (!plan.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("TariffPlan", "id", planId);
        }

        return mapPlanToResponse(plan);
    }

    @Transactional
    public TariffRuleResponse createTariffRule(UUID companyId, CreateTariffRuleRequest request) {
        TariffPlan plan = tariffPlanRepository.findById(request.getTariffPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "id", request.getTariffPlanId()));

        // Ensure plan belongs to the company
        if (!plan.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("TariffPlan", "id", request.getTariffPlanId());
        }

        TariffRule rule = new TariffRule();
        rule.setTariffPlan(plan);
        rule.setAmount(request.getAmount());
        rule.setHouseType(request.getHouseType());
        rule.setPickupFrequencyPerWeek(request.getPickupFrequencyPerWeek());

        // Validate and set zone if provided
        if (request.getZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.getZoneId()));

            if (!zone.getWasteCompany().getId().equals(companyId)) {
                throw new ResourceNotFoundException("Zone", "id", request.getZoneId());
            }

            rule.setZone(zone);
        }

        // Check for duplicate rule
        if (tariffRuleRepository.existsByTariffPlan_IdAndZone_IdAndHouseType(
                plan.getId(),
                request.getZoneId(),
                request.getHouseType())) {
            throw new BadRequestException("A tariff rule already exists for this combination");
        }

        TariffRule savedRule = tariffRuleRepository.save(rule);
        return mapRuleToResponse(savedRule);
    }

    @Transactional(readOnly = true)
    public Page<TariffRuleResponse> getTariffRulesByPlan(UUID planId, UUID companyId, Pageable pageable) {
        TariffPlan plan = tariffPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("TariffPlan", "id", planId));

        if (!plan.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("TariffPlan", "id", planId);
        }

        Page<TariffRule> rules = tariffRuleRepository.findByTariffPlan_Id(planId, pageable);
        return rules.map(this::mapRuleToResponse);
    }

    @Transactional
    public void deleteTariffRule(UUID ruleId, UUID companyId) {
        TariffRule rule = tariffRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("TariffRule", "id", ruleId));

        if (!rule.getTariffPlan().getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("TariffRule", "id", ruleId);
        }

        tariffRuleRepository.delete(rule);
    }

    private TariffPlanResponse mapPlanToResponse(TariffPlan plan) {
        long ruleCount = tariffRuleRepository.countByTariffPlan_Id(plan.getId());

        return TariffPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .billingFrequency(plan.getBillingFrequency())
                .effectiveFrom(plan.getEffectiveFrom())
                .effectiveTo(plan.getEffectiveTo())
                .companyId(plan.getWasteCompany().getId())
                .companyName(plan.getWasteCompany().getName())
                .ruleCount(ruleCount)
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private TariffRuleResponse mapRuleToResponse(TariffRule rule) {
        TariffRuleResponse.TariffRuleResponseBuilder builder = TariffRuleResponse.builder()
                .id(rule.getId())
                .tariffPlanId(rule.getTariffPlan().getId())
                .tariffPlanName(rule.getTariffPlan().getName())
                .houseType(rule.getHouseType())
                .pickupFrequencyPerWeek(rule.getPickupFrequencyPerWeek())
                .amount(rule.getAmount())
                .createdAt(rule.getCreatedAt());

        if (rule.getZone() != null) {
            builder.zoneId(rule.getZone().getId())
                    .zoneSector(rule.getZone().getSector())
                    .zoneCell(rule.getZone().getCell())
                    .zoneVillage(rule.getZone().getVillage());
        }

        return builder.build();
    }
}
