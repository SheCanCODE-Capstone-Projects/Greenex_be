package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.PickupSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PickupSessionRepository extends JpaRepository<PickupSession, UUID> {
}
