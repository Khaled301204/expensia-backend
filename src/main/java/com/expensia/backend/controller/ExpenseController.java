package com.expensia.backend.controller;

import com.expensia.backend.dto.request.ExpenseRequest;
import com.expensia.backend.dto.request.ParseExpenseRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
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
    public ApiResponse<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return ApiResponse.success(
                "Expense created successfully",
                expenseService.createExpense(request)
        );
    }

    @GetMapping
    public ApiResponse<List<ExpenseResponse>> getMyExpenses() {
        return ApiResponse.success(
                "Expenses retrieved successfully",
                expenseService.getMyExpenses()
        );
    }

    @PostMapping("/parse-text")
    public ApiResponse<ExpenseResponse> parseTextExpense(
            @Valid @RequestBody ParseExpenseRequest request
    ) {
        return ApiResponse.success(
                "Expense parsed and created successfully",
                expenseService.parseAndCreateExpense(request.getText())
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ApiResponse.success("Expense deleted successfully", null);
    }
}