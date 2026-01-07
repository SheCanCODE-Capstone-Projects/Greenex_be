package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {
    long countByZone_Id(UUID zoneId);
    boolean existsByCode(String code);
    List<Household> findByZone_Id(UUID zoneId);
}
