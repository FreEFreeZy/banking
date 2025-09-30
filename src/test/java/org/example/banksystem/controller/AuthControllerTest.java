package org.example.banksystem.controller;

import org.example.banksystem.dto.AuthRequest;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.security.JwtTokenProvider;
import org.example.banksystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnSuccess_WhenValidCredentials() {
        // Arrange
        AuthRequest request = new AuthRequest("testuser", "password");
        User user = new User("testuser", "encodedPassword", Role.ROLE_USER);

        when(userService.exists("testuser")).thenReturn(true);
        when(userService.get("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createToken(any(), any())).thenReturn("jwt-token");

        // Act
        var result = authController.login(request, response);

        // Assert
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertEquals("Success", result.getBody().get("Response"));
        verify(response).addCookie(any());
    }

    @Test
    void login_ShouldReturnNotFound_WhenUserNotExists() {
        // Arrange
        AuthRequest request = new AuthRequest("unknown", "password");
        when(userService.exists("unknown")).thenReturn(false);

        // Act
        var result = authController.login(request, response);

        // Assert
        assertEquals(404, result.getStatusCode().value());
        assertEquals("User not found", result.getBody().get("Response"));
        verify(userService, never()).get(any());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenWrongPassword() {
        // Arrange
        AuthRequest request = new AuthRequest("testuser", "wrongpassword");
        User user = new User("testuser", "encodedPassword", Role.ROLE_USER);

        when(userService.exists("testuser")).thenReturn(true);
        when(userService.get("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act
        var result = authController.login(request, response);

        // Assert
        assertEquals(401, result.getStatusCode().value());
        assertEquals("Wrong password", result.getBody().get("Response"));
    }

    @Test
    void register_ShouldReturnSuccess_WhenNewUser() {
        // Arrange
        AuthRequest request = new AuthRequest("newuser", "password");
        when(userService.exists("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        var result = authController.register(request);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("User registered!", result.getBody().get("Response"));
        verify(userService).save(any(User.class));
    }

    @Test
    void register_ShouldReturnUnauthorized_WhenUserExists() {
        // Arrange
        AuthRequest request = new AuthRequest("existinguser", "password");
        when(userService.exists("existinguser")).thenReturn(true);

        // Act
        var result = authController.register(request);

        // Assert
        assertEquals(401, result.getStatusCode().value());
        assertEquals("User already exists!", result.getBody().get("Response"));
        verify(userService, never()).save(any(User.class));
    }
}
