package com.expensia.backend.service.auth;

import com.expensia.backend.dto.request.LoginRequest;
import com.expensia.backend.dto.request.RegisterRequest;
import com.expensia.backend.dto.response.AuthResponse;
import com.expensia.backend.model.entity.User;
import com.expensia.backend.model.enums.RiskPreference;
import com.expensia.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("0123456789");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken("user@example.com")).thenReturn("mock-token");

        AuthResponse response = authService.register(request);

        assertEquals("mock-token", response.getToken());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(RiskPreference.MEDIUM, response.getRiskPreference());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("pass");
        request.setName("Someone");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withRiskPreference_preservesIt() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("pass");
        request.setName("User");
        request.setRiskPreference(RiskPreference.HIGH);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(anyString())).thenReturn("token");

        AuthResponse response = authService.register(request);

        assertEquals(RiskPreference.HIGH, response.getRiskPreference());
    }

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .userId(1L)
                .email("user@example.com")
                .passwordHash("hashed")
                .name("Test User")
                .riskPreference(RiskPreference.MEDIUM)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("user@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("user@example.com", response.getEmail());
    }

    @Test
    void login_userNotFound_throwsIllegalArgumentException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("any");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_throwsIllegalArgumentException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpass");

        User user = User.builder()
                .userId(1L)
                .email("user@example.com")
                .passwordHash("hashed")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(anyString());
    }
}
