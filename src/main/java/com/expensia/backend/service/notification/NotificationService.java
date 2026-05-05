package com.expensia.backend.service.notification;

import com.expensia.backend.model.entity.Notification;
import com.expensia.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(Long userId, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow();
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    // Budget Alert Logic
    public void checkBudgetAlerts(Long userId, Long budgetId) {
        // TODO: Check if expenses > budget * 0.8
        // If yes, create budget alert notification
        createNotification(userId, "BUDGET_ALERT",
                "Budget Alert",
                "You've spent 80% of your Food budget");
    }
}
