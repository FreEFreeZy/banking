package org.example.banksystem.service;

import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.repository.UserRepository;
import org.example.banksystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса аутентификации AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "encodedPassword123";
    private final String JWT_TOKEN = "jwt.token.here";

    @BeforeEach
    void setUp() {
        testUser = new User(USERNAME, ENCODED_PASSWORD, Role.ROLE_USER);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = authService.loadUserByUsername(USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_WhenUserNotExists_ShouldReturnNull() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act
        UserDetails result = authService.loadUserByUsername(USERNAME);

        // Assert
        assertNull(result);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnResponseCookie() {
        // Arrange
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.createToken(USERNAME, testUser.getAuthorities())).thenReturn(JWT_TOKEN);

        // Act
        ResponseCookie result = authService.login(USERNAME, PASSWORD);

        // Assert
        assertNotNull(result);
        assertEquals("Authorization", result.getName());
        assertEquals(JWT_TOKEN, result.getValue());
        assertEquals("/", result.getPath());
        assertFalse(result.isSecure());
        assertEquals(3600, result.getMaxAge().getSeconds());
        assertTrue(result.isHttpOnly());
        assertEquals("Strict", result.getSameSite());

        verify(userRepository).findById(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
        verify(jwtTokenProvider).createToken(USERNAME, testUser.getAuthorities());
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.login(USERNAME, PASSWORD));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).createToken(anyString(), any());
    }

    @Test
    void login_WithWrongPassword_ShouldThrowUserWrongCredentialsException() {
        // Arrange
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> authService.login(USERNAME, PASSWORD));

        assertEquals("Wrong password", exception.getMessage());
        verify(userRepository).findById(USERNAME);
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
        verify(jwtTokenProvider, never()).createToken(anyString(), any());
    }

    @Test
    void registerUser_WithNewUser_ShouldSaveUser() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.registerUser(USERNAME, PASSWORD);

        // Assert
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(ENCODED_PASSWORD) &&
                        user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    void registerUser_WhenUserAlreadyExists_ShouldThrowUserWrongCredentialsException() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> authService.registerUser(USERNAME, PASSWORD));

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.registerUser(USERNAME, rawPassword);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    void login_ShouldUseCorrectAuthoritiesForTokenCreation() {
        // Arrange
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenProvider.createToken(USERNAME, testUser.getAuthorities())).thenReturn(JWT_TOKEN);

        // Act
        authService.login(USERNAME, PASSWORD);

        // Assert
        verify(jwtTokenProvider).createToken(USERNAME, testUser.getAuthorities());
    }

    @Test
    void registerUser_ShouldSetDefaultRoleAsUser() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.registerUser(USERNAME, PASSWORD);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.ROLE_USER
        ));
    }
}