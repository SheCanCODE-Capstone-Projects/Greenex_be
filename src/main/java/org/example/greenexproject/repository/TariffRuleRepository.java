package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.TariffRule;
import org.example.greenexproject.model.enums.HouseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TariffRuleRepository extends JpaRepository<TariffRule, UUID> {
    List<TariffRule> findByTariffPlan_Id(UUID tariffPlanId);

    Optional<TariffRule> findByTariffPlan_IdAndZone_IdAndHouseType(
            UUID tariffPlanId,
            UUID zoneId,
            HouseType houseType
    );

    @Query("SELECT tr FROM TariffRule tr " +
            "JOIN tr.tariffPlan tp " +
            "WHERE tp.wasteCompany.id = :companyId " +
            "AND tp.status = 'ACTIVE' " +
            "AND tr.zone.id = :zoneId " +
            "AND tr.houseType = :houseType " +
            "AND tp.effectiveFrom <= CURRENT_DATE " +
            "AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= CURRENT_DATE)")
    Optional<TariffRule> findActiveRuleForHousehold(
            @Param("companyId") UUID companyId,
            @Param("zoneId") UUID zoneId,
            @Param("houseType") HouseType houseType
    );

    Page<TariffRule> findByTariffPlan_Id(UUID tariffPlanId, Pageable pageable);

    long countByTariffPlan_Id(UUID tariffPlanId);

    boolean existsByTariffPlan_IdAndZone_IdAndHouseType(UUID tariffPlanId, UUID zoneId, HouseType houseType);

}
