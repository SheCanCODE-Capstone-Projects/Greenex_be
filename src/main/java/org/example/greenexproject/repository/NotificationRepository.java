package org.example.greenexproject.repository;

import org.example.greenexproject.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Get all notifications for a specific user
    List<Notification> findByRecipientUser_Id(UUID userId);
}
