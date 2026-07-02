package com.expensia.backend.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken("user@example.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String email = "test@expensia.com";
        String token = jwtService.generateToken(email);
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_returnsTrueForFreshToken() {
        String token = jwtService.generateToken("user@example.com");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtService.generateToken("user@example.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThrows(Exception.class, () -> jwtService.isTokenValid(tampered));
    }

    @Test
    void generateToken_differentEmailsProduceDifferentTokens() {
        String t1 = jwtService.generateToken("a@example.com");
        String t2 = jwtService.generateToken("b@example.com");
        assertNotEquals(t1, t2);
    }
}
