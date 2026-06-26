package com.expensia.backend.repository;

import com.expensia.backend.model.entity.Notification;
import com.expensia.backend.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndTypeAndIsReadFalse(
            Long userId,
            NotificationType type
    );

    boolean existsByUserIdAndTypeAndMessageContainingAndIsReadFalse(
            Long userId,
            NotificationType type,
            String messagePart
    );
}