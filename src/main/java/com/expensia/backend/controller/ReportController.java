package com.expensia.backend.controller;

import com.expensia.backend.dto.response.AIInsightsResponse;
import com.expensia.backend.dto.response.AIRecommendationResponse;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.ReportResponse;
import com.expensia.backend.service.report.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        String csv = reportService.exportCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expense-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] pdf = reportService.exportPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expense-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}