package com.expensia.backend.controller;

import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.NotificationResponse;
import com.expensia.backend.service.notification.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.success(
                "Notifications retrieved successfully",
                notificationService.getMyNotifications()
        );
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications() {
        return ApiResponse.success(
                "Unread notifications retrieved successfully",
                notificationService.getUnreadNotifications()
        );
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Object> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success("Notification marked as read", null);
    }
}