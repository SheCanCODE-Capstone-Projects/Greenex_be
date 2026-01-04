package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.ComplaintRequest;
import org.example.greenexproject.dto.request.NotificationRequest;
import org.example.greenexproject.dto.response.NotificationResponse;
import org.example.greenexproject.model.entity.Complaint;
import org.example.greenexproject.model.entity.Household;
import org.example.greenexproject.model.enums.NotificationType;
import org.example.greenexproject.repository.ComplaintRepository;
import org.example.greenexproject.repository.HouseholdRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final HouseholdRepository householdRepository;
    private final NotificationService notificationService;
    private final NotificationWebSocketService webSocketService; // added WebSocket

    @Override
    public Complaint createComplaint(ComplaintRequest request) {

        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() ->
                        new RuntimeException("Household not found"));

        Complaint complaint = Complaint.builder()
                .household(household)
                .type(request.getType())
                .description(request.getDescription())
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        //  Create DB notification
        NotificationResponse notificationResponse = notificationService.createNotification(
                NotificationRequest.builder()
                        .companyId(household.getWasteCompany().getId())
                        .type(NotificationType.COMPLAINT)
                        .message("New complaint from household " + household.getCode())
                        .complaintId(savedComplaint.getId())
                        .build()
        );

        //  Send real-time WebSocket notification
        webSocketService.sendToCompany(
                household.getWasteCompany().getId(),
                notificationResponse
        );

        return savedComplaint;
    }
}
