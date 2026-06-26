package com.expensia.backend.service.notification;

import com.expensia.backend.dto.response.NotificationResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Notification;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.NotificationRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type
    ) {

        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getMyNotifications() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadNotifications() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    public boolean hasUnreadNotification(Long userId, NotificationType type) {
        return notificationRepository.existsByUserIdAndTypeAndIsReadFalse(
                userId,
                type
        );
    }

    public boolean hasUnreadNotificationContaining(
            Long userId,
            NotificationType type,
            String messagePart
    ) {
        return notificationRepository.existsByUserIdAndTypeAndMessageContainingAndIsReadFalse(
                userId,
                type,
                messagePart
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}