package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.CompanyUser;
import org.example.greenexproject.model.enums.CompanyRole;
import org.example.greenexproject.model.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, UUID> {

    Optional<CompanyUser> findBySystemUser_IdAndWasteCompany_Id(UUID systemUserId, UUID companyId);

    Optional<CompanyUser> findBySystemUser_Id(UUID systemUserId);

    List<CompanyUser> findByWasteCompany_IdAndRole(UUID companyId, CompanyRole role);

    Page<CompanyUser> findByWasteCompany_IdAndSystemUser_UserType(UUID companyId, UserType userType, Pageable pageable);


    @Query("SELECT cu FROM CompanyUser cu WHERE cu.wasteCompany.id = :companyId AND cu.role = :role")
    List<CompanyUser> findManagersByCompany(@Param("companyId") UUID companyId, @Param("role") CompanyRole role);

    // ✅ NEW: Find the single manager of a company
    Optional<CompanyUser> findByWasteCompanyIdAndRole(UUID companyId, CompanyRole role);
}
