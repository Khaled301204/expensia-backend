//package com.expensia.backend.dto.response;
//
//import com.expensia.backend.dto.request.ExpenseRequest;
//import com.expensia.backend.dto.response.ApiResponse;
//import com.expensia.backend.model.entity.Expense;
//import com.expensia.backend.service.expense.ExpenseService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/expenses")
//@RequiredArgsConstructor
//public class ExpenseController {
//
//    private final ExpenseService expenseService;
//
//    @PostMapping
//    public ApiResponse<Expense> addExpense(
//            @RequestHeader("userId") Long userId,
//            @RequestBody ExpenseRequest request) {
//        Expense expense = expenseService.createExpense(userId, request);
//        return ApiResponse.success(expense);
//    }
//
//    @GetMapping
//    public ApiResponse<List<Expense>> getExpenses(
//            @RequestHeader("userId") Long userId) {
//        List<Expense> expenses = expenseService.getExpenses(userId);
//        return ApiResponse.success(expenses);
//    }
//
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> deleteExpense(@PathVariable Long id) {
//        expenseService.deleteExpense(id);
//        return ApiResponse.success(null);
//    }
//}