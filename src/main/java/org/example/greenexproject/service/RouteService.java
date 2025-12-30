package org.example.greenexproject.service;

import lombok.RequiredArgsConstructor;
import org.example.greenexproject.dto.request.CreateRouteRequest;
import org.example.greenexproject.dto.request.UpdateRouteRequest;
import org.example.greenexproject.dto.response.RouteResponse;
import org.example.greenexproject.exception.BadRequestException;
import org.example.greenexproject.exception.ResourceNotFoundException;
import org.example.greenexproject.model.entity.Route;
import org.example.greenexproject.model.entity.WasteCompany;
import org.example.greenexproject.model.entity.Zone;
import org.example.greenexproject.repository.RouteRepository;
import org.example.greenexproject.repository.WasteCompanyRepository;
import org.example.greenexproject.repository.ZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final ZoneRepository zoneRepository;
    private final WasteCompanyRepository wasteCompanyRepository;

    @Transactional
    public RouteResponse createRoute(UUID companyId, CreateRouteRequest request) {
        WasteCompany company = wasteCompanyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("WasteCompany", "id", companyId));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.getZoneId()));

        // Ensure zone belongs to the company
        if (!zone.getWasteCompany().getId().equals(companyId)) {
            throw new BadRequestException("Zone does not belong to your company");
        }

        Route route = Route.builder()
                .wasteCompany(company)
                .zone(zone)
                .name(request.getName())
                .dayOfWeek(request.getDayOfWeek())
                .shift(request.getShift())
                .build();

        Route savedRoute = routeRepository.save(route);
        return mapToResponse(savedRoute);
    }

    @Transactional(readOnly = true)
    public Page<RouteResponse> listRoutes(UUID companyId, Pageable pageable) {
        return routeRepository.findByWasteCompany_Id(companyId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RouteResponse getRouteById(UUID companyId, UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        // Ensure route belongs to the company
        if (!route.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Route", "id", routeId);
        }

        return mapToResponse(route);
    }

    @Transactional
    public RouteResponse updateRoute(UUID companyId, UUID routeId, UpdateRouteRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        // Ensure route belongs to the company
        if (!route.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Route", "id", routeId);
        }

        if (request.getName() != null) {
            route.setName(request.getName());
        }
        if (request.getDayOfWeek() != null) {
            route.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getShift() != null) {
            route.setShift(request.getShift());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }

        Route updatedRoute = routeRepository.save(route);
        return mapToResponse(updatedRoute);
    }

    @Transactional
    public void deleteRoute(UUID companyId, UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        // Ensure route belongs to the company
        if (!route.getWasteCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Route", "id", routeId);
        }

        routeRepository.delete(route);
    }

    private RouteResponse mapToResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .zoneId(route.getZone().getId())
                .zoneName(route.getZone().getVillage() + ", " + route.getZone().getSector())
                .name(route.getName())
                .dayOfWeek(route.getDayOfWeek())
                .shift(route.getShift())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}
