package com.expensia.backend.service.budget;

import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.Category;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.CategoryRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    public BudgetAlertService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            NotificationService notificationService
    ) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
    }

    public void checkBudgetAlert(Long userId, Expense expense) {

        if (expense.getCategoryId() == null) {
            return;
        }

        List<Budget> budgets = budgetRepository.findByUserIdAndCategoryId(
                userId,
                expense.getCategoryId()
        );

        for (Budget budget : budgets) {

            BigDecimal totalSpent =
                    expenseRepository.sumByUserIdAndCategoryIdAndDateBetween(
                            userId,
                            budget.getCategoryId(),
                            budget.getStartDate().atStartOfDay(),
                            budget.getEndDate().plusDays(1).atStartOfDay()
                    );

            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            BigDecimal limit = budget.getLimitAmount();

            double threshold = budget.getAlertThreshold() == null
                    ? 0.8
                    : budget.getAlertThreshold();

            BigDecimal thresholdAmount = limit.multiply(BigDecimal.valueOf(threshold));

            String categoryName = categoryRepository
                    .findById(budget.getCategoryId())
                    .map(Category::getName)
                    .orElse("Category " + budget.getCategoryId());

            if (totalSpent.compareTo(limit) >= 0) {

                if (!notificationService.hasUnreadNotificationContaining(
                        userId,
                        NotificationType.BUDGET_EXCEEDED,
                        categoryName
                )) {
                    notificationService.createNotification(
                            userId,
                            "Budget exceeded",
                            "You exceeded your "
                                    + categoryName
                                    + " budget. Spent: "
                                    + totalSpent
                                    + " / Limit: "
                                    + limit,
                            NotificationType.BUDGET_EXCEEDED
                    );
                }

                return;
            }

            if (totalSpent.compareTo(thresholdAmount) >= 0) {

                if (!notificationService.hasUnreadNotificationContaining(
                        userId,
                        NotificationType.BUDGET_WARNING,
                        categoryName
                )) {
                    notificationService.createNotification(
                            userId,
                            "Budget warning",
                            "You used "
                                    + Math.round(threshold * 100)
                                    + "% of your "
                                    + categoryName
                                    + " budget. Spent: "
                                    + totalSpent
                                    + " / Limit: "
                                    + limit,
                            NotificationType.BUDGET_WARNING
                    );
                }
            }
        }
    }
}