package org.example.greenexproject.service;

import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.enums.RegistrationStatus;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WasteCompanyService {

    private final WasteCompanyRepository repository;

    public WasteCompanyService(WasteCompanyRepository repository) {
        this.repository = repository;
    }


    public Page<WasteCompany> getPendingCompanies(Pageable pageable) {
        return repository.findByRegistrationStatus(RegistrationStatus.PENDING, pageable);
    }


    public WasteCompany approveCompany(UUID companyId) {
        WasteCompany company = repository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not Found"));
        company.setRegistrationStatus(RegistrationStatus.APPROVED);
        return repository.save(company);
    }

    public WasteCompany rejectCompany(UUID companyId) {
        WasteCompany company = repository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setRegistrationStatus(RegistrationStatus.REJECTED);
        return repository.save(company);
    }
}
