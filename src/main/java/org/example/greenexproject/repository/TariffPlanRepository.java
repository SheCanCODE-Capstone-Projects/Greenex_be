package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TariffPlanRepository extends JpaRepository<RouteStop, UUID> {
}
