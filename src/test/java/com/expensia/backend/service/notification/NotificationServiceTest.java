package com.expensia.backend.service.notification;

import com.expensia.backend.dto.response.NotificationResponse;
import com.expensia.backend.exception.ResourceNotFoundException;
import com.expensia.backend.model.entity.Notification;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        User user = User.builder().userId(1L).email("user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Notification notification(Long id, String message, boolean isRead) {
        Notification n = new Notification();
        n.setNotificationId(id);
        n.setUserId(1L);
        n.setTitle("Test");
        n.setMessage(message);
        n.setType(NotificationType.GENERAL);
        n.setIsRead(isRead);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Test
    void createNotification_savesNotificationWithCorrectFields() {
        notificationService.createNotification(1L, "Budget Warning", "80% spent", NotificationType.BUDGET_WARNING);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("Budget Warning", saved.getTitle());
        assertEquals("80% spent", saved.getMessage());
        assertEquals(NotificationType.BUDGET_WARNING, saved.getType());
    }

    @Test
    void markAsRead_setsIsReadTrue() {
        Notification n = notification(5L, "msg", false);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(5L);

        assertTrue(n.getIsRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_notFound_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(99L));
    }

    @Test
    void getMyNotifications_returnsAllForUser() {
        List<Notification> list = List.of(
                notification(1L, "msg1", false),
                notification(2L, "msg2", true)
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(list);

        List<NotificationResponse> result = notificationService.getMyNotifications();

        assertEquals(2, result.size());
    }

    @Test
    void getUnreadNotifications_returnsOnlyUnread() {
        List<Notification> unread = List.of(notification(1L, "unread", false));
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L)).thenReturn(unread);

        List<NotificationResponse> result = notificationService.getUnreadNotifications();

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
    }

    @Test
    void hasUnreadNotification_delegatesToRepository() {
        when(notificationRepository.existsByUserIdAndTypeAndIsReadFalse(1L, NotificationType.BUDGET_EXCEEDED))
                .thenReturn(true);

        assertTrue(notificationService.hasUnreadNotification(1L, NotificationType.BUDGET_EXCEEDED));
    }

    @Test
    void hasUnreadNotificationContaining_delegatesToRepository() {
        when(notificationRepository.existsByUserIdAndTypeAndMessageContainingAndIsReadFalse(
                1L, NotificationType.BUDGET_WARNING, "Food"))
                .thenReturn(false);

        assertFalse(notificationService.hasUnreadNotificationContaining(1L, NotificationType.BUDGET_WARNING, "Food"));
    }
}
