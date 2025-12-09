package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    Page<Zone> findByWasteCompany_Id(UUID companyId, Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT z FROM Zone z WHERE z.sector = :sector AND z.cell = :cell AND z.village = :village")
    Optional<Zone> findBySectorAndCellAndVillage(
            @Param("sector") String sector,
            @Param("cell") String cell,
            @Param("village") String village
    );

    Optional<Zone> findByCode(String code);
}
