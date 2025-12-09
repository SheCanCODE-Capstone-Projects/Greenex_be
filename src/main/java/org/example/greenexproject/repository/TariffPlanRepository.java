package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.RouteStop;
import org.example.greenexproject.model.entity.TariffPlan;
import org.example.greenexproject.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TariffPlanRepository extends JpaRepository<TariffPlan, UUID> {
    Page<TariffPlan> findByWasteCompany_Id(UUID companyId, Pageable pageable);

    List<TariffPlan> findByWasteCompany_IdAndStatus(UUID companyId, UserStatus status);

    @Query("SELECT tp FROM TariffPlan tp WHERE tp.wasteCompany.id = :companyId AND tp.status = :status " +
            "AND tp.effectiveFrom <= :date AND (tp.effectiveTo IS NULL OR tp.effectiveTo >= :date)")
    List<TariffPlan> findActiveByCompanyAndDate(
            @Param("companyId") UUID companyId,
            @Param("status") UserStatus status,
            @Param("date") LocalDate date
    );

    Page<TariffPlan> findByWasteCompany_IdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID companyId, LocalDate effectiveFrom, LocalDate effectiveTo, Pageable pageable);

}
