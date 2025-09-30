package org.example.banksystem.service;

import org.example.banksystem.dto.UserResponse;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getRepo_ShouldReturnUserRepository() {
        // Act
        var result = userService.getRepo();

        // Assert
        assertEquals(userRepository, result);
    }

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String username = "testuser";
        User expectedUser = new User(username, "password", Role.ROLE_USER);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        var result = userService.loadUserByUsername(username);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnNull_WhenUserNotExists() {
        // Arrange
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        var result = userService.loadUserByUsername(username);

        // Assert
        assertNull(result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void parseUser_ShouldReturnUserResponse() {
        // Arrange
        User user = new User("testuser", "encodedPassword", Role.ROLE_ADMIN);

        // Act
        UserResponse result = userService.parseUser(user);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getEncryptedPassword());
        assertEquals("ROLE_ADMIN", result.getRole());
    }

    @Test
    void get_ShouldCallRepositoryGetReferenceById() {
        // Arrange
        String username = "testuser";
        User expectedUser = new User();
        when(userRepository.getReferenceById(username)).thenReturn(expectedUser);

        // Act
        User result = userService.get(username);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).getReferenceById(username);
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
                new User("user1", "pass1", Role.ROLE_USER),
                new User("user2", "pass2", Role.ROLE_ADMIN)
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.getAll();

        // Assert
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // Arrange
        User user = new User("testuser", "password", Role.ROLE_USER);

        // Act
        userService.save(user);

        // Assert
        verify(userRepository).save(user);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        String username = "testuser";

        // Act
        userService.delete(username);

        // Assert
        verify(userRepository).deleteById(username);
    }

    @Test
    void exists_ShouldCallRepositoryExistsById() {
        // Arrange
        String username = "testuser";
        when(userRepository.existsById(username)).thenReturn(true);

        // Act
        boolean result = userService.exists(username);

        // Assert
        assertTrue(result);
        verify(userRepository).existsById(username);
    }
}