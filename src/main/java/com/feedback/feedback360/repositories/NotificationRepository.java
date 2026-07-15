package com.feedback.feedback360.repositories;

import com.feedback.feedback360.entities.Notification;
import com.feedback.feedback360.enums.NotificationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatusAndSentDateBefore(NotificationStatus status, LocalDateTime cutoff);
}
