package com.expensia.backend.controller;

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
}