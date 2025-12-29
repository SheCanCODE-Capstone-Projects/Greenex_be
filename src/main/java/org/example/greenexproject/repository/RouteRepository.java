package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {
    Page<Route> findByWasteCompany_Id(UUID companyId, Pageable pageable);

    List<Route> findByZone_Id(UUID zoneId);

    List<Route> findByWasteCompany_IdAndZone_Id(UUID companyId, UUID zoneId);
}
