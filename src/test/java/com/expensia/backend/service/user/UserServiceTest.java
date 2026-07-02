package com.expensia.backend.service.user;

import com.expensia.backend.dto.request.UpdateUserProfileRequest;
import com.expensia.backend.dto.response.UserResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.RiskPreference;
import com.expensia.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .userId(1L)
                .email("user@example.com")
                .name("Ahmed")
                .phone("0123456789")
                .riskPreference(RiskPreference.MEDIUM)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyProfile_returnsCurrentUserData() {
        UserResponse response = userService.getMyProfile();

        assertEquals(1L, response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Ahmed", response.getName());
        assertEquals("0123456789", response.getPhone());
        assertEquals(RiskPreference.MEDIUM, response.getRiskPreference());
    }

    @Test
    void getMyProfile_nullRiskPreference_defaultsToMedium() {
        User userWithNullRisk = User.builder()
                .userId(2L)
                .email("x@y.com")
                .name("Test")
                .riskPreference(null)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userWithNullRisk, null, Collections.emptyList())
        );

        UserResponse response = userService.getMyProfile();

        assertEquals(RiskPreference.MEDIUM, response.getRiskPreference());
    }

    @Test
    void updateMyProfile_updatesName() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        setField(request, "name", "New Name");

        User updatedUser = User.builder()
                .userId(1L).email("user@example.com")
                .name("New Name").phone("0123456789")
                .riskPreference(RiskPreference.MEDIUM).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserResponse response = userService.updateMyProfile(request);

        assertEquals("New Name", response.getName());
        verify(userRepository).save(any());
    }

    @Test
    void updateMyProfile_updatesPhone() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        setField(request, "phone", "0987654321");

        User updatedUser = User.builder()
                .userId(1L).email("user@example.com")
                .name("Ahmed").phone("0987654321")
                .riskPreference(RiskPreference.MEDIUM).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserResponse response = userService.updateMyProfile(request);

        assertEquals("0987654321", response.getPhone());
    }

    @Test
    void updateMyProfile_updatesRiskPreference() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        setField(request, "riskPreference", RiskPreference.HIGH);

        User updatedUser = User.builder()
                .userId(1L).email("user@example.com")
                .name("Ahmed").phone("0123456789")
                .riskPreference(RiskPreference.HIGH).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserResponse response = userService.updateMyProfile(request);

        assertEquals(RiskPreference.HIGH, response.getRiskPreference());
    }

    @Test
    void updateMyProfile_nullFields_doesNotOverwrite() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateMyProfile(request);

        assertEquals("Ahmed", response.getName());
        assertEquals("0123456789", response.getPhone());
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
