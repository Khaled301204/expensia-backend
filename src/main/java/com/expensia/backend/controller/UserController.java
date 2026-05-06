package com.expensia.backend.controller;

import com.expensia.backend.dto.response.UserResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/user/me")
    public UserResponse getCurrentUser() {
        User user = SecurityUtil.getCurrentUser();

        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhone(user.getPhone());

        return response;
    }
}
