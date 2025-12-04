package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WasteCompanyRepository extends JpaRepository<WasteCompany, UUID> {
    Page<WasteCompany> findByRegistrationStatus(RegistrationStatus status, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByContractNumber(String contractNumber);
}
