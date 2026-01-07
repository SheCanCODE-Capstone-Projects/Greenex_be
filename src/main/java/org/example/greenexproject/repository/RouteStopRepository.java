package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.RouteStop;
import org.example.greenexproject.model.enums.StopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    List<RouteStop> findByPickupSession_IdOrderBySequenceNumberAsc(UUID pickupSessionId);
    List<RouteStop> findByPickupSession_IdAndStatusNot(UUID pickupSessionId, StopStatus status);


    @Query("SELECT COUNT(rs) FROM RouteStop rs WHERE rs.pickupSession.id = :sessionId AND rs.status = :status")
    long countBySessionAndStatus(@Param("sessionId") UUID sessionId, @Param("status") StopStatus status);


}
