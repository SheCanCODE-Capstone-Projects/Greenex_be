package org.example.greenexproject.service;


import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.example.greenexproject.dto.request.RejectCompanyRequest;
import org.example.greenexproject.dto.response.WasteCompanyResponse;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.CompanyUser;
import org.example.greenexproject.model.entity.Notification;
import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.enums.CompanyRole;
import org.example.greenexproject.model.enums.NotificationType;
import org.example.greenexproject.model.enums.RegistrationStatus;
import org.example.greenexproject.model.enums.UserStatus;
import org.example.greenexproject.repository.CompanyUserRepository;
import org.example.greenexproject.repository.NotificationRepository;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final WasteCompanyRepository wasteCompanyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final NotificationRepository notificationRepository;

    public Page<WasteCompanyResponse> getPendingCompanies(Pageable pageable) {
        Page<WasteCompany> companies = wasteCompanyRepository
                .findByRegistrationStatus(RegistrationStatus.PENDING, pageable);

        return companies.map(this::mapToResponse);
    }

    public void approveCompany(UUID companyId) throws BadRequestException {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        if (company.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Company is not in pending status");
        }


        company.setRegistrationStatus(RegistrationStatus.APPROVED);
        company.setStatus(UserStatus.ACTIVE);
        wasteCompanyRepository.save(company);


        List<CompanyUser> managers = companyUserRepository
                .findByWasteCompany_IdAndRole(companyId, CompanyRole.MANAGER);

        for (CompanyUser manager : managers) {
            manager.setStatus(UserStatus.ACTIVE);
            manager.getSystemUser().setStatus(UserStatus.ACTIVE);
            companyUserRepository.save(manager);


            Notification notification = Notification.builder()
                    .recipientUser(manager.getSystemUser())
                    .type(NotificationType.COMPANY_APPROVED)
                    .title("Company Approved")
                    .message("Your company '" + company.getName() +
                            "' has been approved! You can now start configuring your services.")
                    .build();
            notificationRepository.save(notification);
        }
    }

    public void rejectCompany(UUID companyId, RejectCompanyRequest request) throws BadRequestException {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        if (company.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Company is not in pending status");
        }


        company.setRegistrationStatus(RegistrationStatus.REJECTED);
        wasteCompanyRepository.save(company);


        List<CompanyUser> managers = companyUserRepository
                .findByWasteCompany_IdAndRole(companyId, CompanyRole.MANAGER);

        for (CompanyUser manager : managers) {
            Notification notification = Notification.builder()
                    .recipientUser(manager.getSystemUser())
                    .type(NotificationType.COMPANY_REJECTED)
                    .title("Company Registration Rejected")
                    .message("Your Company Registration for '" + company.getName() +
                            "' has been rejected. Reason: " + request.getReason())
                    .build();
            notificationRepository.save(notification);
        }
    }

    public Page<WasteCompanyResponse> getApprovedCompanies(Pageable pageable) {
        Page<WasteCompany> companies = wasteCompanyRepository
                .findByRegistrationStatus(RegistrationStatus.APPROVED, pageable);

        return companies.map(this::mapToResponse);
    }

    private WasteCompanyResponse mapToResponse(WasteCompany company) {
        return WasteCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .contractNumber(company.getContractNumber())
                .sectorCoverage(company.getSectorCoverage())
                .status(company.getStatus())
                .registrationStatus(company.getRegistrationStatus())
                .createdByName(company.getCreatedBy().getFullName())
                .createdByEmail(company.getCreatedBy().getEmail())
                .remaDocumentUrl(company.getRemaDocumentUrl())
                .cityOfKigaliDocumentUrl(company.getCityOfKigaliDocumentUrl())
                .rdbDocumentUrl(company.getRdbDocumentUrl())
                .createdAt(company.getCreatedAt())
                .build();
    }
}