package com.expensia.backend.controller;

import com.expensia.backend.dto.request.UpdateUserProfileRequest;
import com.expensia.backend.dto.response.ApiResponse;
import com.expensia.backend.dto.response.UserResponse;
import com.expensia.backend.service.user.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.success(
                "User retrieved successfully",
                userService.getMyProfile()
        );
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(
            @RequestBody UpdateUserProfileRequest request
    ) {
        return ApiResponse.success(
                "Profile updated successfully",
                userService.updateMyProfile(request)
        );
    }
}