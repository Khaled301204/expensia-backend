//package com.expensia.backend.controller;
//
//import com.expensia.backend.dto.response.ApiResponse;
//import com.expensia.backend.dto.response.ReportResponse;
//import com.expensia.backend.service.report.ReportService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/reports")
//@RequiredArgsConstructor
//public class ReportController {
//
//    private final ReportService reportService;
//
//    @GetMapping("/monthly")
//    public ApiResponse<ReportResponse> getMonthlyReport(
//            @RequestHeader("userId") Long userId,
//            @RequestParam int year,
//            @RequestParam int month) {
//        ReportResponse report = reportService.getMonthlyReport(userId, year, month);
//        return ApiResponse.success(report);
//    }
//}
