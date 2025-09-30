package org.example.banksystem.controller;

import org.example.banksystem.dto.CardResponse;
import org.example.banksystem.dto.TransferRequest;
import org.example.banksystem.entity.Card;
import org.example.banksystem.service.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CardController cardController;

    @Test
    void getCards_ShouldReturnUserCards() {
        // Arrange
        String username = "testuser";
        Card card1 = new Card(1, "encrypted123", username, new Date(), "ACTIVE", 1000.0);
        Card card2 = new Card(2, "encrypted456", username, new Date(), "ACTIVE", 500.0);
        CardResponse cardResponse1 = new CardResponse(1, "****123", username.toUpperCase(), new Date(), "ACTIVE");
        CardResponse cardResponse2 = new CardResponse(2, "****456", username.toUpperCase(), new Date(), "ACTIVE");

        when(principal.getName()).thenReturn(username);
        when(cardService.getByUser(username)).thenReturn(Arrays.asList(card1, card2));
        when(cardService.parseCard(card1)).thenReturn(cardResponse1);
        when(cardService.parseCard(card2)).thenReturn(cardResponse2);

        // Act
        var result = cardController.getCards(principal);

        // Assert
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertEquals(2, result.getBody().get("Cards").size());
        verify(cardService).getByUser(username);
    }

    @Test
    void blockCard_ShouldReturnSuccess_WhenUserOwnsCard() {
        // Arrange
        String username = "testuser";
        Integer cardId = 1;

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, cardId)).thenReturn(true);

        // Act
        var result = cardController.blockCard(cardId, principal);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Card blocked", result.getBody().get("message"));
        verify(cardService).blockCard(cardId);
    }

    @Test
    void blockCard_ShouldReturnForbidden_WhenUserNotOwnsCard() {
        // Arrange
        String username = "testuser";
        Integer cardId = 1;

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, cardId)).thenReturn(false);

        // Act
        var result = cardController.blockCard(cardId, principal);

        // Assert
        assertEquals(403, result.getStatusCode().value());
        assertEquals("Card does not belong to this account", result.getBody().get("message"));
        verify(cardService, never()).blockCard(anyInt());
    }

    @Test
    void transfer_ShouldReturnSuccess_WhenValidTransfer() {
        // Arrange
        String username = "testuser";
        TransferRequest request = new TransferRequest(1, 2, 100.0);
        Card fromCard = new Card(1, "encrypted1", username, new Date(), "ACTIVE", 500.0);
        Card toCard = new Card(2, "encrypted2", username, new Date(), "ACTIVE", 200.0);

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, 1)).thenReturn(true);
        when(cardService.validateOwner(username, 2)).thenReturn(true);
        when(cardService.get(1)).thenReturn(fromCard);
        when(cardService.get(2)).thenReturn(toCard);

        // Act
        var result = cardController.transfer(request, principal);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Money transferred", result.getBody().get("message"));
        verify(cardService).transfer(1, 2, 100.0);
    }

    @Test
    void transfer_ShouldReturnForbidden_WhenUserNotOwnsCards() {
        // Arrange
        String username = "testuser";
        TransferRequest request = new TransferRequest(1, 2, 100.0);

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, 1)).thenReturn(false);

        // Act
        var result = cardController.transfer(request, principal);

        // Assert
        assertEquals(403, result.getStatusCode().value());
        assertEquals("Card does not belong to this account", result.getBody().get("message"));
        verify(cardService, never()).transfer(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void transfer_ShouldReturnBadRequest_WhenCardBlocked() {
        // Arrange
        String username = "testuser";
        TransferRequest request = new TransferRequest(1, 2, 100.0);
        Card blockedCard = new Card(1, "encrypted1", username, new Date(), "BLOCKED", 500.0);

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, 1)).thenReturn(true);
        when(cardService.validateOwner(username, 2)).thenReturn(true);
        when(cardService.get(1)).thenReturn(blockedCard);

        // Act
        var result = cardController.transfer(request, principal);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("Card is blocked", result.getBody().get("message"));
        verify(cardService, never()).transfer(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void transfer_ShouldReturnBadRequest_WhenInsufficientBalance() {
        // Arrange
        String username = "testuser";
        TransferRequest request = new TransferRequest(1, 2, 1000.0);
        Card fromCard = new Card(1, "encrypted1", username, new Date(), "ACTIVE", 500.0);
        Card toCard = new Card(2, "encrypted2", username, new Date(), "ACTIVE", 200.0);

        when(principal.getName()).thenReturn(username);
        when(cardService.validateOwner(username, 1)).thenReturn(true);
        when(cardService.validateOwner(username, 2)).thenReturn(true);
        when(cardService.get(1)).thenReturn(fromCard);
        when(cardService.get(2)).thenReturn(toCard);

        // Act
        var result = cardController.transfer(request, principal);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("Not enough balance", result.getBody().get("message"));
        verify(cardService, never()).transfer(anyInt(), anyInt(), anyDouble());
    }
}
