package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.Payment;
import org.example.greenexproject.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByWasteCompany_IdAndPeriodMonth(UUID companyId, String periodMonth);

    Page<Payment> findByWasteCompany_Id(UUID companyId, Pageable pageable);

    Page<Payment> findByWasteCompany_IdAndPeriodMonth(UUID companyId, String periodMonth, Pageable pageable);

    Page<Payment> findByWasteCompany_IdAndStatus(UUID companyId, PaymentStatus status, Pageable pageable);
}
