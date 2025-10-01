package org.example.banksystem.controller;

import org.example.banksystem.dto.request.AuthRequest;
import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для контроллера аутентификации AuthController
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String JWT_TOKEN = "jwt.token.here";

    @Test
    void login_WithValidCredentials_ShouldReturnResponseWithCookie() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        ResponseCookie expectedCookie = ResponseCookie.from("Authorization", JWT_TOKEN)
                .path("/")
                .secure(false)
                .maxAge(3600)
                .httpOnly(true)
                .sameSite("Strict")
                .build();

        when(authService.login(USERNAME, PASSWORD)).thenReturn(expectedCookie);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = authController.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Authorization success", responseBody.message());
        assertNull(responseBody.data());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("Authorization=" + JWT_TOKEN));

        verify(authService).login(USERNAME, PASSWORD);
    }

    @Test
    void login_WhenUserNotFound_ShouldPropagateException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        when(authService.login(USERNAME, PASSWORD))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authController.login(authRequest));

        assertEquals("User not found", exception.getMessage());
        verify(authService).login(USERNAME, PASSWORD);
    }

    @Test
    void login_WhenWrongPassword_ShouldPropagateException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        when(authService.login(USERNAME, PASSWORD))
                .thenThrow(new UserWrongCredentialsException("Wrong password"));

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> authController.login(authRequest));

        assertEquals("Wrong password", exception.getMessage());
        verify(authService).login(USERNAME, PASSWORD);
    }

    @Test
    void login_ShouldExtractCredentialsFromRequest() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        ResponseCookie expectedCookie = ResponseCookie.from("Authorization", JWT_TOKEN).build();

        when(authService.login(USERNAME, PASSWORD)).thenReturn(expectedCookie);

        // Act
        authController.login(authRequest);

        // Assert
        verify(authService).login(USERNAME, PASSWORD);
    }

    @Test
    void register_WithValidData_ShouldReturnSuccessResponse() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = authController.register(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("User successfully registered", responseBody.message());
        assertNull(responseBody.data());

        verify(authService).registerUser(USERNAME, PASSWORD);
    }

    @Test
    void register_WhenUserAlreadyExists_ShouldPropagateException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        doThrow(new UserWrongCredentialsException("User already exists"))
                .when(authService).registerUser(USERNAME, PASSWORD);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> authController.register(authRequest));

        assertEquals("User already exists", exception.getMessage());
        verify(authService).registerUser(USERNAME, PASSWORD);
    }

    @Test
    void register_ShouldExtractCredentialsFromRequest() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        // Act
        authController.register(authRequest);

        // Assert
        verify(authService).registerUser(USERNAME, PASSWORD);
    }

    @Test
    void login_ShouldSetCorrectCookieProperties() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        ResponseCookie expectedCookie = ResponseCookie.from("Authorization", JWT_TOKEN)
                .path("/")
                .secure(false)
                .maxAge(3600)
                .httpOnly(true)
                .sameSite("Strict")
                .build();

        when(authService.login(USERNAME, PASSWORD)).thenReturn(expectedCookie);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = authController.login(authRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("Authorization=" + JWT_TOKEN));
        assertTrue(setCookieHeader.contains("Path=/"));
        assertTrue(setCookieHeader.contains("Max-Age=3600"));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        assertTrue(setCookieHeader.contains("SameSite=Strict"));
    }

    @Test
    void login_WithDifferentCredentials_ShouldPassCorrectParameters() {
        // Arrange
        String differentUsername = "differentuser";
        String differentPassword = "differentpass";
        AuthRequest authRequest = new AuthRequest(differentUsername, differentPassword);
        ResponseCookie expectedCookie = ResponseCookie.from("Authorization", JWT_TOKEN).build();

        when(authService.login(differentUsername, differentPassword)).thenReturn(expectedCookie);

        // Act
        authController.login(authRequest);

        // Assert
        verify(authService).login(differentUsername, differentPassword);
    }

    @Test
    void register_WithDifferentCredentials_ShouldPassCorrectParameters() {
        // Arrange
        String differentUsername = "newuser";
        String differentPassword = "newpass";
        AuthRequest authRequest = new AuthRequest(differentUsername, differentPassword);

        // Act
        authController.register(authRequest);

        // Assert
        verify(authService).registerUser(differentUsername, differentPassword);
    }

    @Test
    void login_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        ResponseCookie expectedCookie = ResponseCookie.from("Authorization", JWT_TOKEN).build();

        when(authService.login(USERNAME, PASSWORD)).thenReturn(expectedCookie);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = authController.login(authRequest);

        // Assert
        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Authorization success", responseBody.message());
        assertNotNull(responseBody.timestamp());
    }

    @Test
    void register_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = authController.register(authRequest);

        // Assert
        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("User successfully registered", responseBody.message());
        assertNotNull(responseBody.timestamp());
    }
}