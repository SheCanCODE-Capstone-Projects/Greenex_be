package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.greenexproject.dto.request.CreateDriverRequest;
import org.example.greenexproject.dto.response.DriverResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.CompanyUser;
import org.example.greenexproject.model.entity.SystemUser;
import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.enums.CompanyRole;
import org.example.greenexproject.model.enums.UserStatus;
import org.example.greenexproject.model.enums.UserType;
import org.example.greenexproject.repository.CompanyUserRepository;
import org.example.greenexproject.repository.SystemUserRepository;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverManagementService {
    private final SystemUserRepository systemUserRepository;
    private final CompanyUserRepository companyUserRepository;
    private final WasteCompanyRepository wasteCompanyRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Company Manager creates a driver account for their company
     */
    @Transactional
    public DriverResponse createDriver(UUID managerId, CreateDriverRequest request) {
        // Get manager's company
        CompanyUser managerCompanyUser = companyUserRepository.findBySystemUser_Id(managerId)
                .orElseThrow(() -> new BadRequestException("Manager not associated with any company"));

        WasteCompany company = managerCompanyUser.getWasteCompany();

        if (company.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Company is not active. Cannot create driver accounts.");
        }

        // Validate unique email and phone
        if (systemUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (systemUserRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }

        // Create system user for driver
        SystemUser driverUser = SystemUser.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .userType(UserType.COMPANY_DRIVER)
                .status(UserStatus.ACTIVE)
                .build();

        driverUser = systemUserRepository.save(driverUser);

        // Create company user record linking driver to company
        CompanyUser companyUser = CompanyUser.builder()
                .systemUser(driverUser)
                .wasteCompany(company)
                .role(CompanyRole.DRIVER)
                .licenseNumber(request.getLicenseNumber())
                .build();

        companyUser = companyUserRepository.save(companyUser);

        log.info("Driver created: {} for company: {}", driverUser.getEmail(), company.getName());

        return mapToResponse(driverUser, companyUser);
    }

    /**
     * Get all drivers for a company (for manager)
     */
    @Transactional(readOnly = true)
    public Page<DriverResponse> getCompanyDrivers(UUID managerId, Pageable pageable) {
        // Get manager's company
        CompanyUser managerCompanyUser = companyUserRepository.findBySystemUser_Id(managerId)
                .orElseThrow(() -> new BadRequestException("Manager not associated with any company"));

        UUID companyId = managerCompanyUser.getWasteCompany().getId();

        // Get all drivers for the company
        Page<CompanyUser> drivers = companyUserRepository.findByWasteCompany_IdAndSystemUser_UserType(
                companyId, UserType.COMPANY_DRIVER, pageable);

        return drivers.map(cu -> mapToResponse(cu.getSystemUser(), cu));
    }

    /**
     * Get driver details
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID managerId, UUID driverId) {
        // Get manager's company
        CompanyUser managerCompanyUser = companyUserRepository.findBySystemUser_Id(managerId)
                .orElseThrow(() -> new BadRequestException("Manager not associated with any company"));

        UUID companyId = managerCompanyUser.getWasteCompany().getId();

        // Get driver
        CompanyUser driverCompanyUser = companyUserRepository.findBySystemUser_Id(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        // Verify driver belongs to same company
        if (!driverCompanyUser.getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Driver does not belong to your company");
        }

        return mapToResponse(driverCompanyUser.getSystemUser(), driverCompanyUser);
    }

    /**
     * Update driver status (activate/deactivate)
     */
    @Transactional
    public void updateDriverStatus(UUID managerId, UUID driverId, UserStatus status) {
        // Get manager's company
        CompanyUser managerCompanyUser = companyUserRepository.findBySystemUser_Id(managerId)
                .orElseThrow(() -> new BadRequestException("Manager not associated with any company"));

        UUID companyId = managerCompanyUser.getWasteCompany().getId();

        // Get driver
        SystemUser driver = systemUserRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        if (driver.getUserType() != UserType.COMPANY_DRIVER) {
            throw new BadRequestException("User is not a driver");
        }

        CompanyUser driverCompanyUser = companyUserRepository.findBySystemUser_Id(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        // Verify driver belongs to same company
        if (!driverCompanyUser.getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Driver does not belong to your company");
        }

        driver.setStatus(status);
        systemUserRepository.save(driver);

        log.info("Driver {} status updated to {} by manager {}", driverId, status, managerId);
    }

    /**
     * Admin assigns a driver to a company
     */
    @Transactional
    public void assignDriverToCompany(UUID driverId, UUID companyId) {
        SystemUser driver = systemUserRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverId));

        if (driver.getUserType() != UserType.COMPANY_DRIVER) {
            throw new BadRequestException("User is not a driver");
        }

        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Check if driver already assigned to a company
        companyUserRepository.findBySystemUser_Id(driverId).ifPresent(cu -> {
            throw new BadRequestException("Driver is already assigned to company: " +
                    cu.getWasteCompany().getName());
        });

        // Create company user record
        CompanyUser companyUser = CompanyUser.builder()
                .systemUser(driver)
                .wasteCompany(company)
                .role(CompanyRole.DRIVER)
                .build();

        companyUserRepository.save(companyUser);

        log.info("Driver {} assigned to company {} by admin", driverId, companyId);
    }

    private DriverResponse mapToResponse(SystemUser driver, CompanyUser companyUser) {
        return DriverResponse.builder()
                .id(driver.getId())
                .fullName(driver.getFullName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .licenseNumber(companyUser.getLicenseNumber())
                .companyName(companyUser.getWasteCompany().getName())
                .companyId(companyUser.getWasteCompany().getId())
                .status(driver.getStatus().name())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}
