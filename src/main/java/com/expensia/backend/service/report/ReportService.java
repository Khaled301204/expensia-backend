package com.expensia.backend.service.report;

import com.expensia.backend.dto.response.AIInsightsResponse;
import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.exception.AIServiceException;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.Income;
import com.expensia.backend.model.entity.SavingGoal;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.entity.Wallet;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.repository.GoalRepository;
import com.expensia.backend.repository.IncomeRepository;
import com.expensia.backend.repository.WalletRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final GoalRepository goalRepository;
    private final AIServiceClient aiServiceClient;
    private final WalletRepository walletRepository;

    public ReportService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            GoalRepository goalRepository,
            AIServiceClient aiServiceClient,
            WalletRepository walletRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.goalRepository = goalRepository;
        this.aiServiceClient = aiServiceClient;
        this.walletRepository = walletRepository;
    }

    public ReportResponse generateMonthlyReport() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        LocalDateTime start =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses =
                expenseRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        List<Income> incomes =
                incomeRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Expense expense : expenses) {

            totalExpenses = totalExpenses.add(expense.getAmount());

            String category =
                    expense.getCategoryName() != null
                            ? expense.getCategoryName()
                            : "Other";

            categoryBreakdown.put(
                    category,
                    categoryBreakdown.getOrDefault(category, BigDecimal.ZERO)
                            .add(expense.getAmount())
            );
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new ReportResponse(
                totalIncome,
                totalExpenses,
                balance,
                categoryBreakdown
        );
    }

    public AIRecommendationResponse getRecommendations() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return getRecommendationsForUser(currentUser);
    }

    public AIRecommendationResponse getRecommendationsForUser(User user) {

        LocalDateTime start =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = start.plusMonths(1);

        List<Expense> expenses =
                expenseRepository.findByUserIdAndDateBetween(
                        user.getUserId(),
                        start,
                        end
                );

        List<Income> incomes =
                incomeRepository.findByUserIdAndDateBetween(
                        user.getUserId(),
                        start,
                        end
                );

        List<SavingGoal> goals =
                goalRepository.findByUserId(user.getUserId());

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());

            String category = expense.getCategoryName() != null
                    ? expense.getCategoryName()
                    : "Other";

            categoryBreakdown.put(
                    category,
                    categoryBreakdown.getOrDefault(category, BigDecimal.ZERO)
                            .add(expense.getAmount())
            );
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
        }

        List<Map<String, Object>> goalData = goals.stream()
                .map(goal -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", goal.getName());
                    map.put("target_amount", goal.getTargetAmount());
                    map.put("current_amount", goal.getCurrentAmount());

                    if (goal.getDeadline() != null) {
                        map.put("deadline", goal.getDeadline().toString());
                    }

                    return map;
                })
                .toList();

        BigDecimal currentSavings = walletRepository
                .findByUserId(user.getUserId())
                .map(Wallet::getCurrentSavings)
                .orElse(BigDecimal.ZERO);

        Map<String, Object> request = new HashMap<>();

        request.put("user_id", user.getUserId());
        request.put("monthly_income", totalIncome);
        request.put("monthly_expenses", totalExpenses);
        request.put("current_savings", currentSavings);
        request.put("risk_preference", "MEDIUM");
        request.put("expense_breakdown", categoryBreakdown);
        request.put("goals", goalData);

        AIRecommendationResponse response =
                aiServiceClient.getRecommendations(request);

        if (response == null || !response.isSuccess()) {
            throw new AIServiceException("Could not generate recommendations");
        }

        return response;
    }

    public AIInsightsResponse getCompleteInsights() {

        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        LocalDateTime start =
                LocalDateTime.now()
                        .minusMonths(6)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0);

        LocalDateTime end = LocalDateTime.now();

        List<Expense> expenses =
                expenseRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        List<Income> incomes =
                incomeRepository.findByUserIdAndDateBetween(
                        currentUser.getUserId(),
                        start,
                        end
                );

        List<SavingGoal> goals =
                goalRepository.findByUserId(currentUser.getUserId());

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        List<Map<String, Object>> transactions = new ArrayList<>();

        for (Expense expense : expenses) {

            totalExpenses = totalExpenses.add(expense.getAmount());

            String category =
                    expense.getCategoryName() != null
                            ? expense.getCategoryName()
                            : "Other";

            categoryBreakdown.put(
                    category,
                    categoryBreakdown.getOrDefault(category, BigDecimal.ZERO)
                            .add(expense.getAmount())
            );

            Map<String, Object> transaction = new HashMap<>();

            transaction.put("date", expense.getDate().toString());
            transaction.put("category", category);
            transaction.put("amount", expense.getAmount());

            transactions.add(transaction);
        }

        for (Income income : incomes) {
            totalIncome = totalIncome.add(income.getAmount());
        }

        List<Map<String, Object>> goalData = goals.stream()
                .map(goal -> {

                    Map<String, Object> map = new HashMap<>();

                    map.put("name", goal.getName());
                    map.put("target_amount", goal.getTargetAmount());
                    map.put("current_amount", goal.getCurrentAmount());

                    if (goal.getDeadline() != null) {
                        map.put("deadline", goal.getDeadline().toString());
                    }

                    return map;
                })
                .toList();

        Map<String, Object> request = new HashMap<>();

        request.put("user_id", currentUser.getUserId());

        request.put("monthly_income", totalIncome);

        request.put("monthly_expenses", totalExpenses);

        BigDecimal currentSavings = walletRepository
                .findByUserId(currentUser.getUserId())
                .map(Wallet::getCurrentSavings)
                .orElse(BigDecimal.ZERO);

        request.put("current_savings", currentSavings);

        request.put("risk_preference", "MEDIUM");

        request.put("expense_breakdown", categoryBreakdown);

        request.put("transactions", transactions);

        request.put("goals", goalData);

        AIInsightsResponse response =
                aiServiceClient.getCompleteInsights(request);

        if (response == null || !response.isSuccess()) {
            throw new AIServiceException("Could not generate insights");
        }

        return response;
    }
}