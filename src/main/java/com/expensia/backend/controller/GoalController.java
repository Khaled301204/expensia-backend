package com.expensia.backend.controller;

import com.expensia.backend.dto.request.AddSavingsRequest;
import com.expensia.backend.dto.request.GoalRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.GoalResponse;
import com.expensia.backend.service.goal.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ApiResponse<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        return ApiResponse.success(
                "Goal created successfully",
                goalService.createGoal(request)
        );
    }

    @GetMapping
    public ApiResponse<List<GoalResponse>> getMyGoals() {
        return ApiResponse.success(
                "Goals retrieved successfully",
                goalService.getMyGoals()
        );
    }

    @PostMapping("/{id}/savings")
    public ApiResponse<GoalResponse> addSavings(
            @PathVariable Long id,
            @Valid @RequestBody AddSavingsRequest request
    ) {
        return ApiResponse.success(
                "Savings added successfully",
                goalService.addSavings(id, request.getAmount())
        );
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<GoalResponse> withdrawSavings(
            @PathVariable Long id,
            @Valid @RequestBody AddSavingsRequest request
    ) {
        return ApiResponse.success(
                "Savings withdrawn successfully",
                goalService.withdrawSavings(id, request.getAmount())
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ApiResponse.success("Goal deleted successfully", null);
    }
}