//package com.expensia.backend.controller;
//
//import com.expensia.backend.dto.response.ApiResponse;
//import com.expensia.backend.model.entity.Income;
//import com.expensia.backend.service.income.IncomeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/incomes")
//@RequiredArgsConstructor
//public class IncomeController {
//
//    private final IncomeService incomeService;
//
//    @PostMapping
//    public ApiResponse<Income> addIncome(
//            @RequestHeader("userId") Long userId,
//            @RequestBody Income income) {
//        Income created = incomeService.createIncome(userId, income);
//        return ApiResponse.success(created);
//    }
//
//    @GetMapping
//    public ApiResponse<List<Income>> getIncomes(
//            @RequestHeader("userId") Long userId) {
//        return ApiResponse.success(incomeService.getIncomes(userId));
//    }
//}
