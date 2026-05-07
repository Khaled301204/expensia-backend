package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.response.AICategorizationResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.service.ai.AIServiceClient;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AIServiceClient aiServiceClient;

    public ExpenseService( ExpenseRepository expenseRepository, AIServiceClient aiServiceClient ) {
        this.expenseRepository = expenseRepository;
        this.aiServiceClient = aiServiceClient;
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
        expense.setCategoryName(aiResponse.getCategory());
        expense.setCategoryConfidence(aiResponse.getConfidence());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setCreatedByVoice(false);

        Expense savedExpense = expenseRepository.save(expense);
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

    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getExpenseId(),
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getCategoryName(),
                expense.getCategoryConfidence(),
                expense.getDate(),
                expense.getDescription(),
                expense.getMerchant(),
                expense.getPaymentMethod(),
                expense.getIsRecurring(),
                expense.getCreatedByVoice()
        );
    }
}