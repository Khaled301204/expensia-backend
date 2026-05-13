package com.expensia.backend.controller;

import com.expensia.backend.dto.request.LoginRequest;
import com.expensia.backend.dto.request.RegisterRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.AuthResponse;
import com.expensia.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ApiResponse.success(
                "User registered successfully",
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.success(
                "Login successful",
                authService.login(request)
        );
    }
}