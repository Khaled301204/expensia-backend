package com.expensia.backend.service.scheduler;

import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.UserRepository;
import com.expensia.backend.service.notification.NotificationService;
import com.expensia.backend.service.report.ReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FinancialSummaryScheduler {

    private final UserRepository userRepository;
    private final ReportService reportService;
    private final NotificationService notificationService;

    public FinancialSummaryScheduler(
            UserRepository userRepository,
            ReportService reportService,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.reportService = reportService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklySummaries() {

        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                AIRecommendationResponse response =
                        reportService.getRecommendationsForUser(user);

                String message = "Financial score: " + response.getOverallScore();

                Map<String, Object> savingRecommendations =
                        response.getSavingRecommendations();

                if (savingRecommendations != null &&
                        savingRecommendations.get("recommendations") instanceof List<?> recommendations &&
                        !recommendations.isEmpty()) {

                    message += ". " + recommendations.get(0).toString();
                }

                notificationService.createNotification(
                        user.getUserId(),
                        "Weekly Financial Summary",
                        message,
                        NotificationType.GENERAL
                );

            } catch (Exception e) {
                System.out.println(
                        "Scheduler error for user "
                                + user.getUserId()
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }
}