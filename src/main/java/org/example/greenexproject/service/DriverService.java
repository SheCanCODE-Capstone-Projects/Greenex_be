package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.response.SessionResponse;
import org.example.greenexproject.dto.response.StopResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.PickupSession;
import org.example.greenexproject.model.entity.RouteStop;
import org.example.greenexproject.model.enums.SessionStatus;
import org.example.greenexproject.model.enums.StopStatus;
import org.example.greenexproject.repository.PickupSessionRepository;
import org.example.greenexproject.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final PickupSessionRepository pickupSessionRepository;
    private final RouteStopRepository routeStopRepository;

    @Transactional(readOnly = true)
    public List<SessionResponse> getTodaySessions(UUID driverUserId) {
        List<PickupSession> sessions = pickupSessionRepository
                .findByDriverUser_IdAndDate(driverUserId, LocalDate.now());

        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getUpcomingSessions(UUID driverUserId) {
        List<PickupSession> sessions = pickupSessionRepository
                .findByDriverUser_IdAndDateAfter(driverUserId, LocalDate.now());

        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StopResponse> getSessionStops(UUID driverUserId, UUID sessionId) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session is assigned to this driver
        if (session.getDriverUser() == null || !session.getDriverUser().getId().equals(driverUserId)) {
            throw new BadRequestException("This session is not assigned to you");
        }

        List<RouteStop> stops = routeStopRepository
                .findByPickupSession_IdOrderBySequenceNumberAsc(sessionId);

        return stops.stream()
                .map(this::mapStopToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StopResponse updateStopStatus(UUID driverUserId, UUID stopId, StopStatus status, String reason) {
        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", stopId));

        // Ensure stop belongs to a session assigned to this driver
        PickupSession session = stop.getPickupSession();
        if (session.getDriverUser() == null || !session.getDriverUser().getId().equals(driverUserId)) {
            throw new BadRequestException("This stop is not assigned to you");
        }

        stop.setStatus(status);
        if (reason != null && !reason.isBlank()) {
            stop.setReason(reason);
        }

        if (status == StopStatus.COMPLETED || status == StopStatus.SKIPPED) {
            stop.setCompletedAt(LocalDateTime.now());
        }

        RouteStop updatedStop = routeStopRepository.save(stop);

        // Update session status if in progress
        if (session.getStatus() == SessionStatus.ASSIGNED || session.getStatus() == SessionStatus.PLANNED) {
            session.setStatus(SessionStatus.IN_PROGRESS);
            pickupSessionRepository.save(session);
        }

        return mapStopToResponse(updatedStop);
    }

    @Transactional
    public SessionResponse completeSession(UUID driverUserId, UUID sessionId) {
        PickupSession session = pickupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("PickupSession", "id", sessionId));

        // Ensure session is assigned to this driver
        if (session.getDriverUser() == null || !session.getDriverUser().getId().equals(driverUserId)) {
            throw new BadRequestException("This session is not assigned to you");
        }

        // Check if all stops are completed or skipped
        List<RouteStop> pendingStops = routeStopRepository
                .findByPickupSession_IdAndStatusNot(sessionId, StopStatus.COMPLETED);

        if (!pendingStops.isEmpty() && pendingStops.stream().anyMatch(s -> s.getStatus() != StopStatus.SKIPPED)) {
            throw new BadRequestException("Cannot complete session with pending stops");
        }

        session.setStatus(SessionStatus.COMPLETED);
        PickupSession completedSession = pickupSessionRepository.save(session);

        return mapToResponse(completedSession);
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
                : "N/A";

        return StopResponse.builder()
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
