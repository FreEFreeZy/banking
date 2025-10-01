package org.example.banksystem.controller;

import org.example.banksystem.dto.request.CardRequest;
import org.example.banksystem.dto.request.UserRequest;
import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.dto.response.CardResponse;
import org.example.banksystem.dto.response.UserResponse;
import org.example.banksystem.exceptions.cards.CardNotFoundException;
import org.example.banksystem.exceptions.cards.CardWrongCredentials;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.service.CardService;
import org.example.banksystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для административного контроллера AdminController
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private final Integer CARD_ID = 1;
    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String ROLE_USER = "ROLE_USER";
    private final String CARD_NUMBER = "1234567812345678";
    private final Date EXPIRY_DATE = new Date();

    // Card test data
    private CardResponse createTestCardResponse() {
        return new CardResponse(
                CARD_ID,
                "************5678",
                USERNAME,
                EXPIRY_DATE,
                "ACTIVE"
        );
    }

    private CardRequest createTestCardRequest() {
        return new CardRequest(
                CARD_ID,
                CARD_NUMBER,
                USERNAME,
                EXPIRY_DATE,
                "ACTIVE",
                1000.0
        );
    }

    // User test data
    private UserResponse createTestUserResponse() {
        return new UserResponse(
                USERNAME,
                "encodedPassword",
                ROLE_USER
        );
    }

    private UserRequest createTestUserRequest() {
        return new UserRequest(
                USERNAME,
                PASSWORD,
                ROLE_USER
        );
    }

    // Card endpoints tests

    @Test
    void getCards_ShouldReturnAllCards() {
        // Arrange
        List<CardResponse> expectedCards = List.of(createTestCardResponse());
        when(cardService.getAllCards()).thenReturn(expectedCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response = adminController.getCards();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Cards", responseBody.message());
        assertEquals(expectedCards, responseBody.data());

        verify(cardService).getAllCards();
    }

    @Test
    void getCards_WhenNoCards_ShouldReturnEmptyList() {
        // Arrange
        when(cardService.getAllCards()).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response = adminController.getCards();

        // Assert
        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.data().isEmpty());
        verify(cardService).getAllCards();
    }

    @Test
    void addCard_WithValidData_ShouldAddCard() {
        // Arrange
        CardRequest cardRequest = createTestCardRequest();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.addCard(cardRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card added", responseBody.message());

        verify(cardService).addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE);
    }

    @Test
    void addCard_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        CardRequest cardRequest = createTestCardRequest();
        doThrow(new CardWrongCredentials("Card number already taken"))
                .when(cardService).addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE);

        // Act & Assert
        CardWrongCredentials exception = assertThrows(CardWrongCredentials.class,
                () -> adminController.addCard(cardRequest));

        assertEquals("Card number already taken", exception.getMessage());
        verify(cardService).addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE);
    }

    @Test
    void updateCard_WithValidData_ShouldUpdateCard() {
        // Arrange
        CardRequest cardRequest = createTestCardRequest();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.updateCard(cardRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card updated", responseBody.message());

        verify(cardService).updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "ACTIVE", 1000.0);
    }

    @Test
    void updateCard_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        CardRequest cardRequest = createTestCardRequest();
        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "ACTIVE", 1000.0);

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> adminController.updateCard(cardRequest));

        assertEquals("Card not found", exception.getMessage());
        verify(cardService).updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "ACTIVE", 1000.0);
    }

    @Test
    void deleteCard_WithValidId_ShouldDeleteCard() {
        // Arrange
        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.deleteCard(CARD_ID);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card successfully deleted", responseBody.message());

        verify(cardService).delete(CARD_ID);
    }

    @Test
    void deleteCard_WhenCardNotFound_ShouldPropagateException() {
        // Arrange
        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).delete(CARD_ID);

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> adminController.deleteCard(CARD_ID));

        assertEquals("Card not found", exception.getMessage());
        verify(cardService).delete(CARD_ID);
    }

    // User endpoints tests

    @Test
    void getUsers_ShouldReturnAllUsers() {
        // Arrange
        List<UserResponse> expectedUsers = List.of(createTestUserResponse());
        when(userService.getAllUsers()).thenReturn(expectedUsers);

        // Act
        ResponseEntity<ApiResponseDTO<List<UserResponse>>> response = adminController.getUsers();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<List<UserResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Users", responseBody.message());
        assertEquals(expectedUsers, responseBody.data());

        verify(userService).getAllUsers();
    }

    @Test
    void getUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponseDTO<List<UserResponse>>> response = adminController.getUsers();

        // Assert
        ApiResponseDTO<List<UserResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.data().isEmpty());
        verify(userService).getAllUsers();
    }

    @Test
    void addUser_WithValidData_ShouldAddUser() {
        // Arrange
        UserRequest userRequest = createTestUserRequest();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.addUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("User successfully added", responseBody.message());

        verify(userService).addUser(USERNAME, PASSWORD, ROLE_USER);
    }

    @Test
    void addUser_WhenUserAlreadyExists_ShouldPropagateException() {
        // Arrange
        UserRequest userRequest = createTestUserRequest();
        doThrow(new UserWrongCredentialsException("User already exists"))
                .when(userService).addUser(USERNAME, PASSWORD, ROLE_USER);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> adminController.addUser(userRequest));

        assertEquals("User already exists", exception.getMessage());
        verify(userService).addUser(USERNAME, PASSWORD, ROLE_USER);
    }

    @Test
    void addUser_WhenInvalidRole_ShouldPropagateException() {
        // Arrange
        UserRequest userRequest = createTestUserRequest();
        doThrow(new UserWrongCredentialsException("Role not found"))
                .when(userService).addUser(USERNAME, PASSWORD, ROLE_USER);

        // Act & Assert
        UserWrongCredentialsException exception = assertThrows(UserWrongCredentialsException.class,
                () -> adminController.addUser(userRequest));

        assertEquals("Role not found", exception.getMessage());
        verify(userService).addUser(USERNAME, PASSWORD, ROLE_USER);
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Arrange
        UserRequest userRequest = createTestUserRequest();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.updateUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("User successfully updated", responseBody.message());

        verify(userService).updateUser(USERNAME, PASSWORD, ROLE_USER);
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldPropagateException() {
        // Arrange
        UserRequest userRequest = createTestUserRequest();
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).updateUser(USERNAME, PASSWORD, ROLE_USER);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminController.updateUser(userRequest));

        assertEquals("User not found", exception.getMessage());
        verify(userService).updateUser(USERNAME, PASSWORD, ROLE_USER);
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = adminController.deleteUser(USERNAME);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("User successfully deleted", responseBody.message());

        verify(userService).delete(USERNAME);
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldPropagateException() {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).delete(USERNAME);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminController.deleteUser(USERNAME));

        assertEquals("User not found", exception.getMessage());
        verify(userService).delete(USERNAME);
    }

    // Edge cases and additional tests

    @Test
    void getCards_WithMultipleCards_ShouldReturnAll() {
        // Arrange
        List<CardResponse> multipleCards = List.of(
                createTestCardResponse(),
                new CardResponse(2, "************1234", "user2", EXPIRY_DATE, "BLOCKED"),
                new CardResponse(3, "************9876", "user3", EXPIRY_DATE, "ACTIVE")
        );
        when(cardService.getAllCards()).thenReturn(multipleCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response = adminController.getCards();

        // Assert
        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.data().size());
        verify(cardService).getAllCards();
    }

    @Test
    void getUsers_WithMultipleUsers_ShouldReturnAll() {
        // Arrange
        List<UserResponse> multipleUsers = List.of(
                createTestUserResponse(),
                new UserResponse("admin", "encodedAdminPass", "ROLE_ADMIN"),
                new UserResponse("user2", "encodedPass2", "ROLE_USER")
        );
        when(userService.getAllUsers()).thenReturn(multipleUsers);

        // Act
        ResponseEntity<ApiResponseDTO<List<UserResponse>>> response = adminController.getUsers();

        // Assert
        ApiResponseDTO<List<UserResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.data().size());
        verify(userService).getAllUsers();
    }

    @Test
    void addCard_WithDifferentData_ShouldPassCorrectParameters() {
        // Arrange
        String differentCardNumber = "9876543210987654";
        String differentCardholder = "differentuser";
        Date differentDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        CardRequest cardRequest = new CardRequest(2, differentCardNumber, differentCardholder, differentDate, "BLOCKED", 500.0);

        // Act
        adminController.addCard(cardRequest);

        // Assert
        verify(cardService).addCard(differentCardNumber, differentCardholder, differentDate);
    }

    @Test
    void updateCard_WithDifferentData_ShouldPassCorrectParameters() {
        // Arrange
        Integer differentCardId = 2;
        String differentCardNumber = "9876543210987654";
        String differentCardholder = "differentuser";
        Date differentDate = new Date(System.currentTimeMillis() + 86400000);
        String differentStatus = "BLOCKED";
        Double differentBalance = 500.0;

        CardRequest cardRequest = new CardRequest(differentCardId, differentCardNumber, differentCardholder, differentDate, differentStatus, differentBalance);

        // Act
        adminController.updateCard(cardRequest);

        // Assert
        verify(cardService).updateCard(differentCardId, differentCardNumber, differentCardholder, differentDate, differentStatus, differentBalance);
    }

    @Test
    void addUser_WithAdminRole_ShouldPassCorrectParameters() {
        // Arrange
        String adminRole = "ROLE_ADMIN";
        UserRequest userRequest = new UserRequest("adminuser", "adminpass", adminRole);

        // Act
        adminController.addUser(userRequest);

        // Assert
        verify(userService).addUser("adminuser", "adminpass", adminRole);
    }

    @Test
    void updateUser_WithAdminRole_ShouldPassCorrectParameters() {
        // Arrange
        String adminRole = "ROLE_ADMIN";
        UserRequest userRequest = new UserRequest("adminuser", "adminpass", adminRole);

        // Act
        adminController.updateUser(userRequest);

        // Assert
        verify(userService).updateUser("adminuser", "adminpass", adminRole);
    }

    @Test
    void deleteUser_WithDifferentUsername_ShouldPassCorrectParameter() {
        // Arrange
        String differentUsername = "differentuser";

        // Act
        adminController.deleteUser(differentUsername);

        // Assert
        verify(userService).delete(differentUsername);
    }

    @Test
    void deleteCard_WithDifferentId_ShouldPassCorrectParameter() {
        // Arrange
        Integer differentCardId = 999;

        // Act
        adminController.deleteCard(differentCardId);

        // Assert
        verify(cardService).delete(differentCardId);
    }

    @Test
    void allEndpoints_ShouldReturnCorrectApiResponseStructure() {
        // Test that all endpoints return proper ApiResponseDTO structure
        when(cardService.getAllCards()).thenReturn(List.of(createTestCardResponse()));
        when(userService.getAllUsers()).thenReturn(List.of(createTestUserResponse()));

        // Test getCards
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> cardsResponse = adminController.getCards();
        ApiResponseDTO<List<CardResponse>> cardsBody = cardsResponse.getBody();
        assertNotNull(cardsBody);
        assertNotNull(cardsBody.timestamp());

        // Test getUsers
        ResponseEntity<ApiResponseDTO<List<UserResponse>>> usersResponse = adminController.getUsers();
        ApiResponseDTO<List<UserResponse>> usersBody = usersResponse.getBody();
        assertNotNull(usersBody);
        assertNotNull(usersBody.timestamp());

        // Test addCard
        CardRequest cardRequest = createTestCardRequest();
        ResponseEntity<ApiResponseDTO<Void>> addCardResponse = adminController.addCard(cardRequest);
        ApiResponseDTO<Void> addCardBody = addCardResponse.getBody();
        assertNotNull(addCardBody);
        assertNotNull(addCardBody.timestamp());

        // Test addUser
        UserRequest userRequest = createTestUserRequest();
        ResponseEntity<ApiResponseDTO<Void>> addUserResponse = adminController.addUser(userRequest);
        ApiResponseDTO<Void> addUserBody = addUserResponse.getBody();
        assertNotNull(addUserBody);
        assertNotNull(addUserBody.timestamp());
    }
}