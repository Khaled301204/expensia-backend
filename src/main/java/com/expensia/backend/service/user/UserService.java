package com.expensia.backend.service.user;

import com.expensia.backend.dto.request.UpdateUserProfileRequest;
import com.expensia.backend.dto.response.UserResponse;
import com.expensia.backend.exception.UnauthorizedException;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.RiskPreference;
import com.expensia.backend.repository.UserRepository;
import com.expensia.backend.util.SecurityUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMyProfile() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        return mapToResponse(currentUser);
    }

    public UserResponse updateMyProfile(UpdateUserProfileRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRiskPreference() != null) {
            user.setRiskPreference(request.getRiskPreference());
        }

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();

        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setRiskPreference(
                user.getRiskPreference() == null
                        ? RiskPreference.MEDIUM
                        : user.getRiskPreference()
        );
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }
}