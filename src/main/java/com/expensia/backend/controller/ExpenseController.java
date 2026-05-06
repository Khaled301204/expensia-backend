package com.expensia.backend.controller;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.model.entity.Expense;
import com.expensia.backend.service.expense.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public Expense createExpense(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.createExpense(request);
    }

    @GetMapping
    public List<Expense> getMyExpenses() {
        return expenseService.getMyExpenses();
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }
}