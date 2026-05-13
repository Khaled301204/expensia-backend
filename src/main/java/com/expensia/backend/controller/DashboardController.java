package com.expensia.backend.controller;

import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.DashboardResponse;
import com.expensia.backend.service.analysis.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard/summary")
    public ApiResponse<DashboardResponse> getSummary() {
        return ApiResponse.success(
                "Dashboard summary retrieved successfully",
                dashboardService.getSummary()
        );
    }
}