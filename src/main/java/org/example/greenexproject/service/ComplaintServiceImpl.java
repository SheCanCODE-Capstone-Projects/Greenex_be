package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ComplaintRequest;
import org.example.greenexproject.dto.response.ComplaintResponse;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.example.greenexproject.model.entity.CompanyUser;
import org.example.greenexproject.model.entity.Complaint;
import org.example.greenexproject.model.entity.Household;
import org.example.greenexproject.model.entity.Notification;
import org.example.greenexproject.model.entity.SystemUser;
import org.example.greenexproject.model.enums.CompanyRole;
import org.example.greenexproject.model.enums.NotificationType;
import org.example.greenexproject.repository.ComplaintRepository;
import org.example.greenexproject.repository.CompanyUserRepository;
import org.example.greenexproject.repository.HouseholdRepository;
import org.example.greenexproject.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final HouseholdRepository householdRepository;
    private final CompanyUserRepository companyUserRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketService webSocketService;

    @Override
    public ComplaintResponse createComplaint(ComplaintRequest request) {

        // Find the household
        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new RuntimeException("Household not found"));

        //  Create and save complaint
        Complaint savedComplaint = complaintRepository.save(
                Complaint.builder()
                        .household(household)
                        .type(request.getType())
                        .description(request.getDescription())
                        .build()
        );

        //  Find the company manager (use correct repository method)
        CompanyUser managerCompanyUser = companyUserRepository.findByWasteCompanyIdAndRole(
                household.getWasteCompany().getId(), CompanyRole.MANAGER
        ).orElseThrow(() -> new RuntimeException("Company manager not found"));

        //  Get the SystemUser for Notification
        SystemUser managerUser = managerCompanyUser.getSystemUser();

        //  Create and save Notification
        Notification notification = Notification.builder()
                .recipientUser(managerUser)
                .type(NotificationType.COMPLAINT)
                .message("New complaint from household " + household.getCode())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Send WebSocket notification
        NotificationResponse notificationResponse = NotificationResponse.builder()
                .id(savedNotification.getId())
                .type(savedNotification.getType().name())
                .message(savedNotification.getMessage())
                .createdAt(savedNotification.getCreatedAt())
                .build();

        webSocketService.sendToCompany(household.getWasteCompany().getId(), notificationResponse);

        //  Return ComplaintResponse
        return ComplaintResponse.builder()
                .id(savedComplaint.getId())
                .type(savedComplaint.getType().name()) // Convert enum to string
                .description(savedComplaint.getDescription())
                .createdAt(savedComplaint.getCreatedAt())
                .householdId(household.getId())
                .wasteCompanyId(household.getWasteCompany().getId())
                .build();
    }
}
