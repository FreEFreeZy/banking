package org.example.banksystem.controller;

import org.example.banksystem.dto.request.TransferRequest;
import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.dto.response.CardResponse;
import org.example.banksystem.entity.User;
import org.example.banksystem.exceptions.cards.CardAccessDeniedException;
import org.example.banksystem.exceptions.cards.CardNotFoundException;
import org.example.banksystem.exceptions.cards.CardNotInService;
import org.example.banksystem.service.CardService;
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
 * Тесты для контроллера операций с картами CardController
 */
@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private final String USERNAME = "testuser";
    private final Integer CARD_ID = 1;
    private final Integer FROM_CARD_ID = 1;
    private final Integer TO_CARD_ID = 2;
    private final Double AMOUNT = 100.0;

    private User createTestUser() {
        return new User(USERNAME, "password", org.example.banksystem.entity.Role.ROLE_USER);
    }

    private CardResponse createTestCardResponse() {
        return new CardResponse(
                CARD_ID,
                "************5678",
                USERNAME,
                new Date(),
                "ACTIVE"
        );
    }

    @Test
    void getCards_WithAuthenticatedUser_ShouldReturnUserCards() {
        // Arrange
        User user = createTestUser();
        List<CardResponse> expectedCards = List.of(createTestCardResponse());

        when(cardService.getCardsByUsername(USERNAME)).thenReturn(expectedCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response =
                cardController.getCards(user);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Cards:", responseBody.message());
        assertEquals(expectedCards, responseBody.data());

        verify(cardService).getCardsByUsername(USERNAME);
    }

    @Test
    void getCards_WhenNoCards_ShouldReturnEmptyList() {
        // Arrange
        User user = createTestUser();
        List<CardResponse> emptyCards = List.of();

        when(cardService.getCardsByUsername(USERNAME)).thenReturn(emptyCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response =
                cardController.getCards(user);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Cards:", responseBody.message());
        assertTrue(responseBody.data().isEmpty());

        verify(cardService).getCardsByUsername(USERNAME);
    }

    @Test
    void getCards_ShouldExtractUsernameFromAuthenticatedUser() {
        // Arrange
        User user = createTestUser();
        List<CardResponse> expectedCards = List.of(createTestCardResponse());

        when(cardService.getCardsByUsername(USERNAME)).thenReturn(expectedCards);

        // Act
        cardController.getCards(user);

        // Assert
        verify(cardService).getCardsByUsername(USERNAME);
    }

    @Test
    void blockCard_WithValidData_ShouldBlockCard() {
        // Arrange
        User user = createTestUser();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response =
                cardController.blockCard(CARD_ID, user);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card successfully blocked", responseBody.message());
        assertNull(responseBody.data());

        verify(cardService).blockCard(CARD_ID, USERNAME);
    }

    @Test
    void blockCard_WhenCardAccessDenied_ShouldPropagateException() {
        // Arrange
        User user = createTestUser();
        doThrow(new CardAccessDeniedException("Access denied"))
                .when(cardService).blockCard(CARD_ID, USERNAME);

        // Act & Assert
        CardAccessDeniedException exception = assertThrows(CardAccessDeniedException.class,
                () -> cardController.blockCard(CARD_ID, user));

        assertEquals("Access denied", exception.getMessage());
        verify(cardService).blockCard(CARD_ID, USERNAME);
    }

    @Test
    void blockCard_WhenCardNotFound_ShouldPropagateException() {
        // Arrange
        User user = createTestUser();
        doThrow(new CardNotFoundException("Card not found"))
                .when(cardService).blockCard(CARD_ID, USERNAME);

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardController.blockCard(CARD_ID, user));

        assertEquals("Card not found", exception.getMessage());
        verify(cardService).blockCard(CARD_ID, USERNAME);
    }

    @Test
    void blockCard_WhenCardNotInService_ShouldPropagateException() {
        // Arrange
        User user = createTestUser();
        doThrow(new CardNotInService("Card not in active status"))
                .when(cardService).blockCard(CARD_ID, USERNAME);

        // Act & Assert
        CardNotInService exception = assertThrows(CardNotInService.class,
                () -> cardController.blockCard(CARD_ID, user));

        assertEquals("Card not in active status", exception.getMessage());
        verify(cardService).blockCard(CARD_ID, USERNAME);
    }

    @Test
    void blockCard_ShouldExtractUsernameFromAuthenticatedUser() {
        // Arrange
        User user = createTestUser();

        // Act
        cardController.blockCard(CARD_ID, user);

        // Assert
        verify(cardService).blockCard(CARD_ID, USERNAME);
    }

    @Test
    void transfer_WithValidData_ShouldTransferFunds() {
        // Arrange
        User user = createTestUser();
        TransferRequest transferRequest = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, AMOUNT);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response =
                cardController.transfer(transferRequest, user);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card successfully transferred", responseBody.message());
        assertNull(responseBody.data());

        verify(cardService).transfer(FROM_CARD_ID, TO_CARD_ID, AMOUNT, USERNAME);
    }

    @Test
    void transfer_WhenCardAccessDenied_ShouldPropagateException() {
        // Arrange
        User user = createTestUser();
        TransferRequest transferRequest = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, AMOUNT);

        doThrow(new CardAccessDeniedException("Access denied"))
                .when(cardService).transfer(FROM_CARD_ID, TO_CARD_ID, AMOUNT, USERNAME);

        // Act & Assert
        CardAccessDeniedException exception = assertThrows(CardAccessDeniedException.class,
                () -> cardController.transfer(transferRequest, user));

        assertEquals("Access denied", exception.getMessage());
        verify(cardService).transfer(FROM_CARD_ID, TO_CARD_ID, AMOUNT, USERNAME);
    }

    @Test
    void transfer_ShouldExtractDataFromRequestAndUser() {
        // Arrange
        User user = createTestUser();
        TransferRequest transferRequest = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, AMOUNT);

        // Act
        cardController.transfer(transferRequest, user);

        // Assert
        verify(cardService).transfer(FROM_CARD_ID, TO_CARD_ID, AMOUNT, USERNAME);
    }

    @Test
    void transfer_WithDifferentAmount_ShouldPassCorrectAmount() {
        // Arrange
        User user = createTestUser();
        Double differentAmount = 500.0;
        TransferRequest transferRequest = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, differentAmount);

        // Act
        cardController.transfer(transferRequest, user);

        // Assert
        verify(cardService).transfer(FROM_CARD_ID, TO_CARD_ID, differentAmount, USERNAME);
    }

    @Test
    void transfer_WithDifferentCardIds_ShouldPassCorrectCardIds() {
        // Arrange
        User user = createTestUser();
        Integer differentFromCardId = 3;
        Integer differentToCardId = 4;
        TransferRequest transferRequest = new TransferRequest(differentFromCardId, differentToCardId, AMOUNT);

        // Act
        cardController.transfer(transferRequest, user);

        // Assert
        verify(cardService).transfer(differentFromCardId, differentToCardId, AMOUNT, USERNAME);
    }

    @Test
    void getCards_WithDifferentUser_ShouldPassCorrectUsername() {
        // Arrange
        String differentUsername = "differentuser";
        User user = new User(differentUsername, "password", org.example.banksystem.entity.Role.ROLE_USER);
        List<CardResponse> expectedCards = List.of(createTestCardResponse());

        when(cardService.getCardsByUsername(differentUsername)).thenReturn(expectedCards);

        // Act
        cardController.getCards(user);

        // Assert
        verify(cardService).getCardsByUsername(differentUsername);
    }

    @Test
    void blockCard_WithDifferentCardId_ShouldPassCorrectCardId() {
        // Arrange
        User user = createTestUser();
        Integer differentCardId = 999;

        // Act
        cardController.blockCard(differentCardId, user);

        // Assert
        verify(cardService).blockCard(differentCardId, USERNAME);
    }

    @Test
    void getCards_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        User user = createTestUser();
        List<CardResponse> expectedCards = List.of(createTestCardResponse());

        when(cardService.getCardsByUsername(USERNAME)).thenReturn(expectedCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response = cardController.getCards(user);

        // Assert
        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Cards:", responseBody.message());
        assertEquals(expectedCards, responseBody.data());
        assertNotNull(responseBody.timestamp());
    }

    @Test
    void blockCard_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        User user = createTestUser();

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = cardController.blockCard(CARD_ID, user);

        // Assert
        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card successfully blocked", responseBody.message());
        assertNull(responseBody.data());
        assertNotNull(responseBody.timestamp());
    }

    @Test
    void transfer_ShouldReturnCorrectApiResponseStructure() {
        // Arrange
        User user = createTestUser();
        TransferRequest transferRequest = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, AMOUNT);

        // Act
        ResponseEntity<ApiResponseDTO<Void>> response = cardController.transfer(transferRequest, user);

        // Assert
        ApiResponseDTO<Void> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.status());
        assertEquals("Card successfully transferred", responseBody.message());
        assertNull(responseBody.data());
        assertNotNull(responseBody.timestamp());
    }

    @Test
    void getCards_WithMultipleCards_ShouldReturnAllCards() {
        // Arrange
        User user = createTestUser();
        List<CardResponse> multipleCards = List.of(
                createTestCardResponse(),
                new CardResponse(2, "************1234", USERNAME, new Date(), "BLOCKED"),
                new CardResponse(3, "************9876", USERNAME, new Date(), "ACTIVE")
        );

        when(cardService.getCardsByUsername(USERNAME)).thenReturn(multipleCards);

        // Act
        ResponseEntity<ApiResponseDTO<List<CardResponse>>> response = cardController.getCards(user);

        // Assert
        ApiResponseDTO<List<CardResponse>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.data().size());
        verify(cardService).getCardsByUsername(USERNAME);
    }
}