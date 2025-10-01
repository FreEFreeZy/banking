package org.example.banksystem.service;

import org.example.banksystem.dto.response.UserResponse;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса пользователей UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "encodedPassword123";
    private final String ROLE_USER = "ROLE_USER";
    private final String ROLE_ADMIN = "ROLE_ADMIN";

    @BeforeEach
    void setUp() {
        testUser = new User(USERNAME, ENCODED_PASSWORD, Role.ROLE_USER);
    }

    @Test
    void parseUser_ShouldReturnUserResponse() {
        // Arrange
        // Act
        UserResponse result = userService.parseUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.username());
        assertEquals(ENCODED_PASSWORD, result.encryptedPassword());
        assertEquals("ROLE_USER", result.role());
    }

    @Test
    void getAllUsers_WhenUsersExist_ShouldReturnUserResponses() {
        // Arrange
        List<User> users = List.of(
                testUser,
                new User("admin", "encodedAdminPass", Role.ROLE_ADMIN)
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        UserResponse firstUser = result.get(0);
        assertEquals(USERNAME, firstUser.username());
        assertEquals(ENCODED_PASSWORD, firstUser.encryptedPassword());
        assertEquals("ROLE_USER", firstUser.role());

        UserResponse secondUser = result.get(1);
        assertEquals("admin", secondUser.username());
        assertEquals("encodedAdminPass", secondUser.encryptedPassword());
        assertEquals("ROLE_ADMIN", secondUser.role());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void addUser_WithValidData_ShouldSaveUser() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);
        when(bCryptPasswordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.addUser(USERNAME, PASSWORD, ROLE_USER);

        // Assert
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder).encode(PASSWORD);
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(ENCODED_PASSWORD) &&
                        user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    void addUser_WithAdminRole_ShouldSaveAdminUser() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);
        when(bCryptPasswordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.addUser(USERNAME, PASSWORD, ROLE_ADMIN);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(ENCODED_PASSWORD) &&
                        user.getRole() == Role.ROLE_ADMIN
        ));
    }

    @Test
    void addUser_WhenUserAlreadyExists_ShouldThrowUserWrongCredentialsException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> userService.addUser(USERNAME, PASSWORD, ROLE_USER));

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_WhenInvalidRole_ShouldThrowUserWrongCredentialsException() {
        // Arrange
        String invalidRole = "INVALID_ROLE";
        when(userRepository.existsById(USERNAME)).thenReturn(false);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> userService.addUser(USERNAME, PASSWORD, invalidRole));

        assertEquals("Role not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsById(USERNAME)).thenReturn(false);
        when(bCryptPasswordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.addUser(USERNAME, rawPassword, ROLE_USER);

        // Assert
        verify(bCryptPasswordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        String newPassword = "newPassword123";
        String encodedNewPassword = "encodedNewPassword123";

        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUser(USERNAME, newPassword, ROLE_ADMIN);

        // Assert
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder).encode(newPassword);
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(encodedNewPassword) &&
                        user.getRole() == Role.ROLE_ADMIN
        ));
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(USERNAME, PASSWORD, ROLE_USER));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenInvalidRole_ShouldThrowUserWrongCredentialsException() {
        // Arrange
        String invalidRole = "INVALID_ROLE";
        when(userRepository.existsById(USERNAME)).thenReturn(true);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> userService.updateUser(USERNAME, PASSWORD, invalidRole));

        assertEquals("Role not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldEncodeNewPassword() {
        // Arrange
        String newRawPassword = "newRawPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(bCryptPasswordEncoder.encode(newRawPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUser(USERNAME, newRawPassword, ROLE_USER);

        // Assert
        verify(bCryptPasswordEncoder).encode(newRawPassword);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(encodedNewPassword)
        ));
    }

    @Test
    void delete_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);

        // Act
        userService.delete(USERNAME);

        // Assert
        verify(userRepository).existsById(USERNAME);
        verify(userRepository).deleteById(USERNAME);
    }

    @Test
    void delete_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.delete(USERNAME));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(userRepository, never()).deleteById(anyString());
    }

    @Test
    void addUser_WithAllRoleTypes_ShouldHandleAllValidRoles() {
        // Test all valid role types
        for (Role role : Role.values()) {
            // Arrange
            String username = "user_" + role.name();
            when(userRepository.existsById(username)).thenReturn(false);
            when(bCryptPasswordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.addUser(username, PASSWORD, role.name());

            // Assert
            verify(userRepository).save(argThat(user ->
                    user.getRole() == role
            ));

            // Reset mocks for next iteration
            reset(userRepository, bCryptPasswordEncoder);
        }
    }

    @Test
    void updateUser_WithSameRole_ShouldUpdatePasswordOnly() {
        // Arrange
        String newPassword = "newPassword123";
        String encodedNewPassword = "encodedNewPassword123";

        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateUser(USERNAME, newPassword, ROLE_USER);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(USERNAME) &&
                        user.getPassword().equals(encodedNewPassword) &&
                        user.getRole() == Role.ROLE_USER
        ));
    }
}