package com.expensia.backend.controller;

import com.expensia.backend.dto.response.AIInsightsResponse;
import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.service.report.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ApiResponse<ReportResponse> generateMonthlyReport() {

        return ApiResponse.success(
                "Monthly report generated successfully",
                reportService.generateMonthlyReport()
        );
    }
    @GetMapping("/recommendations")
    public ApiResponse<AIRecommendationResponse> getRecommendations() {

        return ApiResponse.success(
                "Recommendations generated successfully",
                reportService.getRecommendations()
        );
    }

    @GetMapping("/insights")
    public ApiResponse<AIInsightsResponse> getInsights() {

        return ApiResponse.success(
                "Insights generated successfully",
                reportService.getCompleteInsights()
        );
    }
}