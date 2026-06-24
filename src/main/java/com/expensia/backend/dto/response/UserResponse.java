package com.expensia.backend.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;

}
