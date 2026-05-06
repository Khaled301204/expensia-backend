package com.expensia.backend.service.expense;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.repository.ExpenseRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense createExpense(ExpenseRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        Expense expense = new Expense();
        expense.setUserId(currentUser.getUserId()); // 🔥 from JWT
        expense.setAmount(request.getAmount());
        expense.setCategoryId(request.getCategoryId());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        expense.setMerchant(request.getMerchant());
        expense.setPaymentMethod(request.getPaymentMethod());

        return expenseRepository.save(expense);
    }

    public List<Expense> getMyExpenses() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        return expenseRepository.findByUserIdOrderByDateDesc(currentUser.getUserId());
    }

    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }
}