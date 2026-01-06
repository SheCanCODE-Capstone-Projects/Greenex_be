package org.example.greenexproject.repository;
import org.example.greenexproject.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientUser_IdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    List<Notification> findByRecipientUser_IdAndReadAtIsNullOrderByCreatedAtDesc(UUID recipientUserId);

    long countByRecipientUser_IdAndReadAtIsNull(UUID recipientUserId);
    long countByRecipientUser_Id(UUID recipientUserId);
    // Get all notifications for a specific user
    List<Notification> findByRecipientUser_Id(UUID userId);
}
