package com.expensia.backend.controller;

import com.expensia.backend.dto.request.ConfirmVoiceExpenseRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.ExpenseResponse;
import com.expensia.backend.dto.response.VoiceExpensePreviewResponse;
import com.expensia.backend.service.expense.VoiceExpenseService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/expenses")
public class VoiceExpenseController {

    private final VoiceExpenseService voiceExpenseService;

    public VoiceExpenseController(VoiceExpenseService voiceExpenseService) {
        this.voiceExpenseService = voiceExpenseService;
    }

    @PostMapping("/voice")
    public ApiResponse<ExpenseResponse> createVoiceExpense(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", defaultValue = "auto") String language
    ) {
        return ApiResponse.success(
                "Voice expense created successfully",
                voiceExpenseService.createFromVoice(audio, language)
        );
    }

    @PostMapping("/voice/preview")
    public ApiResponse<VoiceExpensePreviewResponse> previewVoiceExpense(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", defaultValue = "auto") String language
    ) {
        return ApiResponse.success(
                "Voice expense preview generated successfully",
                voiceExpenseService.previewFromVoice(audio, language)
        );
    }

    @PostMapping("/voice/confirm")
    public ApiResponse<ExpenseResponse> confirmVoiceExpense(
            @RequestBody ConfirmVoiceExpenseRequest request
    ) {
        return ApiResponse.success(
                "Voice expense confirmed and created successfully",
                voiceExpenseService.confirmVoiceExpense(request)
        );
    }
}