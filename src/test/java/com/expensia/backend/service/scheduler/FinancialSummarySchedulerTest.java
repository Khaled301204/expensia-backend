package com.expensia.backend.service.scheduler;

import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.UserRepository;
import com.expensia.backend.service.notification.NotificationService;
import com.expensia.backend.service.report.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialSummarySchedulerTest {

    @Mock private UserRepository userRepository;
    @Mock private ReportService reportService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private FinancialSummaryScheduler scheduler;

    private User user(Long id) {
        return User.builder().userId(id).email("user" + id + "@example.com").name("User" + id).build();
    }

    private AIRecommendationResponse responseWith(Double score, List<String> recommendations) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        setField(response, "overallScore", score);
        if (recommendations != null) {
            setField(response, "savingRecommendations", Map.of("recommendations", recommendations));
        }
        return response;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateWeeklySummaries_sendsNotificationForEachUser() {
        List<User> users = List.of(user(1L), user(2L));
        when(userRepository.findAll()).thenReturn(users);
        when(reportService.getRecommendationsForUser(any())).thenReturn(responseWith(75.0, List.of()));

        scheduler.generateWeeklySummaries();

        verify(notificationService, times(2)).createNotification(
                anyLong(), eq("Weekly Financial Summary"), anyString(), eq(NotificationType.GENERAL)
        );
    }

    @Test
    void generateWeeklySummaries_includesFirstRecommendationInMessage() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L)));
        when(reportService.getRecommendationsForUser(any()))
                .thenReturn(responseWith(80.0, List.of("Reduce dining expenses")));

        scheduler.generateWeeklySummaries();

        verify(notificationService).createNotification(
                eq(1L),
                eq("Weekly Financial Summary"),
                contains("Reduce dining expenses"),
                eq(NotificationType.GENERAL)
        );
    }

    @Test
    void generateWeeklySummaries_noUsers_doesNothing() {
        when(userRepository.findAll()).thenReturn(List.of());

        scheduler.generateWeeklySummaries();

        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    void generateWeeklySummaries_exceptionForOneUser_continuesForOthers() {
        List<User> users = List.of(user(1L), user(2L));
        when(userRepository.findAll()).thenReturn(users);
        when(reportService.getRecommendationsForUser(users.get(0))).thenThrow(new RuntimeException("AI error"));
        when(reportService.getRecommendationsForUser(users.get(1))).thenReturn(responseWith(60.0, null));

        scheduler.generateWeeklySummaries();

        verify(notificationService, times(1)).createNotification(any(), any(), any(), any());
    }

    @Test
    void generateWeeklySummaries_nullSavingRecommendations_stillSendsNotification() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L)));
        when(reportService.getRecommendationsForUser(any())).thenReturn(responseWith(50.0, null));

        scheduler.generateWeeklySummaries();

        verify(notificationService).createNotification(
                eq(1L), eq("Weekly Financial Summary"), contains("50.0"), eq(NotificationType.GENERAL)
        );
    }
}
