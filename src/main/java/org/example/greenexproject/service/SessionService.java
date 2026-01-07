package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreatePickupSessionRequest;
import org.example.greenexproject.dto.response.SessionResponse;
import org.example.greenexproject.dto.response.StopResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.*;
import org.example.greenexproject.model.enums.SessionStatus;
import org.example.greenexproject.model.enums.StopStatus;
import org.example.greenexproject.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final PickupSessionRepository pickupSessionRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final HouseholdRepository householdRepository;
    private final SystemUserRepository systemUserRepository;

    @Transactional
    public SessionResponse createSession(UUID companyId, CreatePickupSessionRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", request.getRouteId()));

        // Ensure route belongs to the company
        if (!route.getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Route does not belong to your company");
        }

        PickupSession.PickupSessionBuilder sessionBuilder = PickupSession.builder()
                .route(route)
                .date(request.getDate());

        // Assign driver if provided
        if (request.getDriverUserId() != null) {
            SystemUser driver = systemUserRepository.findById(request.getDriverUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", request.getDriverUserId()));
            sessionBuilder.driverUser(driver);
        }

        PickupSession session = pickupSessionRepository.save(sessionBuilder.build());

        // Generate stops automatically
        generateStopsForSession(session);

        return mapToResponse(session);
    }

    @Transactional
    public void generateStopsForSession(PickupSession session) {
        // Get all households in the zone for this route
        List<Household> households = householdRepository.findByZone_Id(session.getRoute().getZone().getId());

        int sequenceNumber = 1;
        LocalTime startTime = session.getRoute().getShift().name().equals("MORNING")
                ? LocalTime.of(8, 0)
                : LocalTime.of(14, 0);

        for (Household household : households) {
            RouteStop stop = RouteStop.builder()
                    .pickupSession(session)
                    .household(household)
                    .sequenceNumber(sequenceNumber++)
                    .plannedTime(startTime.plusMinutes((sequenceNumber - 1) * 15)) // 15 min per stop
                    .build();

            routeStopRepository.save(stop);
        }
    }

    @Transactional
    public SessionResponse assignDriver(UUID companyId, UUID sessionId, UUID driverUserId) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session belongs to the company
        if (!session.getRoute().getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Session does not belong to your company");
        }

        SystemUser driver = systemUserRepository.findById(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", driverUserId));

        session.setDriverUser(driver);
        session.setStatus(SessionStatus.ASSIGNED);

        PickupSession updatedSession = pickupSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> listSessions(UUID companyId, Pageable pageable) {
        // Get all sessions for routes belonging to this company
        return pickupSessionRepository.findByCompanyId(companyId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(UUID companyId, UUID sessionId) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session belongs to the company
        if (!session.getRoute().getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("PickupSession", "id", sessionId);
        }

        return mapToResponse(session);
    }

    @Transactional(readOnly = true)
    public List<StopResponse> getSessionStops(UUID companyId, UUID sessionId) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session belongs to the company
        if (!session.getRoute().getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Session does not belong to your company");
        }

        List<RouteStop> stops = routeStopRepository.findByPickupSession_IdOrderBySequenceNumberAsc(sessionId);
        return stops.stream()
                .map(this::mapStopToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SessionResponse updateSessionStatus(UUID companyId, UUID sessionId, SessionStatus status) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session belongs to the company
        if (!session.getRoute().getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Session does not belong to your company");
        }

        session.setStatus(status);
        PickupSession updatedSession = pickupSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    private SessionResponse mapToResponse(PickupSession session) {
        long totalStops = routeStopRepository.findByPickupSession_IdOrderBySequenceNumberAsc(session.getId()).size();
        long completedStops = routeStopRepository.countBySessionAndStatus(session.getId(), StopStatus.COMPLETED);

        return SessionResponse.builder()
                .id(session.getId())
                .routeId(session.getRoute().getId())
                .routeName(session.getRoute().getName())
                .zoneName(session.getRoute().getZone().getVillage() + ", " + session.getRoute().getZone().getSector())
                .driverUserId(session.getDriverUser() != null ? session.getDriverUser().getId() : null)
                .driverName(session.getDriverUser() != null ? session.getDriverUser().getEmail() : null)
                .date(session.getDate())
                .status(session.getStatus())
                .totalStops((int) totalStops)
                .completedStops((int) completedStops)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private StopResponse mapStopToResponse(RouteStop stop) {
        String citizenName = stop.getHousehold().getCitizenAccount() != null
                ? stop.getHousehold().getCitizenAccount().getCitizenUser().getEmail()
                : "N/A";        return StopResponse.builder()
                .id(stop.getId())
                .householdId(stop.getHousehold().getId())
                .householdAddress(stop.getHousehold().getAddress())
                .citizenName(citizenName)
                .sequenceNumber(stop.getSequenceNumber())
                .plannedTime(stop.getPlannedTime())
                .status(stop.getStatus())
                .reason(stop.getReason())
                .completedAt(stop.getCompletedAt())
                .createdAt(stop.getCreatedAt())
                .build();
    }
}
