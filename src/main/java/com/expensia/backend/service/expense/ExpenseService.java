package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.dto.response.NLPParseResponse;
import com.expensia.backend.model.entity.Budget;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.NotificationType;
import com.expensia.backend.repository.BudgetRepository;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.service.notification.NotificationService;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AIServiceClient aiServiceClient;
    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;

    public ExpenseService( ExpenseRepository expenseRepository, AIServiceClient aiServiceClient, BudgetRepository budgetRepository, NotificationService notificationService ) {
        this.expenseRepository = expenseRepository;
        this.aiServiceClient = aiServiceClient;
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        Expense expense = new Expense();
        expense.setUserId(currentUser.getUserId());
        expense.setAmount(request.getAmount());
        expense.setCategoryId(request.getCategoryId());
        expense.setDescription(request.getDescription());
        AICategorizationResponse aiResponse = aiServiceClient.categorizeExpense( request.getDescription(), request.getMerchant());
        String categoryName = aiResponse.getCategory();
        Double confidence = aiResponse.getConfidence();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        expense.setCategoryName(categoryName);

        expense.setCategoryConfidence(
                confidence == null ? 0.0 : confidence
        );
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setCreatedByVoice(false);

        Expense savedExpense = expenseRepository.save(expense);
        checkBudgetAlert(currentUser.getUserId(), savedExpense);
        return mapToResponse(savedExpense);
    }

    public List<ExpenseResponse> getMyExpenses() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        return expenseRepository.findByUserIdOrderByDateDesc(currentUser.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void checkBudgetAlert(Long userId, Expense expense) {

        if (expense.getCategoryId() == null) {
            return;
        }

        List<Budget> budgets = budgetRepository.findByUserIdAndCategoryId(
                userId,
                expense.getCategoryId()
        );

        for (Budget budget : budgets) {

            BigDecimal totalSpent = expenseRepository.sumByUserIdAndDateBetween(
                    userId,
                    budget.getStartDate().atStartOfDay(),
                    budget.getEndDate().plusDays(1).atStartOfDay()
            );

            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            if (totalSpent.compareTo(budget.getLimitAmount()) > 0) {
                notificationService.createNotification(
                        userId,
                        "Budget exceeded",
                        "You exceeded your budget limit for category ID " + budget.getCategoryId(),
                        NotificationType.BUDGET_EXCEEDED
                );
            }
        }
    }

    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getExpenseId(),
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getCategoryName(),
                expense.getCategoryConfidence() == null ? 0.0 : expense.getCategoryConfidence(),
                expense.getDate(),
                expense.getDescription(),
                expense.getMerchant(),
                expense.getPaymentMethod(),
                expense.getIsRecurring(),
                expense.getCreatedByVoice()
        );
    }

    public ExpenseResponse parseAndCreateExpense(String text) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        NLPParseResponse aiResponse = aiServiceClient.parseExpenseText(text);

        if (aiResponse == null || !aiResponse.isSuccess() || aiResponse.getParsed() == null) {
            throw new RuntimeException("Could not parse expense text");
        }

        NLPParseResponse.ParsedExpense parsed = aiResponse.getParsed();

        Expense expense = new Expense();

        expense.setUserId(currentUser.getUserId());
        expense.setAmount(BigDecimal.valueOf(parsed.getAmount()));
        expense.setMerchant(parsed.getMerchant());
        expense.setDescription(parsed.getDescription());
        Double confidence =
                parsed.getConfidence() != null
                        ? parsed.getConfidence().get("category")
                        : 0.0;

        String categoryName = parsed.getCategory();

        if (confidence == null || confidence < 0.40) {
            categoryName = "Other";
        }

        expense.setCategoryName(categoryName);

        expense.setCategoryConfidence(
                confidence == null ? 0.0 : confidence
        );

        if (parsed.getDate() != null) {
            expense.setDate(LocalDate.parse(parsed.getDate()).atStartOfDay());
        }

        expense.setPaymentMethod("CASH");
        expense.setIsRecurring(false);
        expense.setCreatedByVoice(true);

        Expense savedExpense = expenseRepository.save(expense);

        checkBudgetAlert(currentUser.getUserId(), savedExpense);

        return mapToResponse(savedExpense);
    }
}