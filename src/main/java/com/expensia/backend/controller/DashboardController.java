package com.expensia.backend.controller;

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
    public DashboardResponse getSummary() {
        return dashboardService.getSummary();
    }
}