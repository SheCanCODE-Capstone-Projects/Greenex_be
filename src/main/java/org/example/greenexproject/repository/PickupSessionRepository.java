package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.PickupSession;
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
public interface PickupSessionRepository extends JpaRepository<PickupSession, UUID> {
    @Query("SELECT ps FROM PickupSession ps WHERE ps.route.wasteCompany.id = :companyId")
    Page<PickupSession> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    List<PickupSession> findByDriverUser_IdAndDate(UUID driverUserId, LocalDate date);

    List<PickupSession> findByDriverUser_IdAndDateAfter(UUID driverUserId, LocalDate date);

}
