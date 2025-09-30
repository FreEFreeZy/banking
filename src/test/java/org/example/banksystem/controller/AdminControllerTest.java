package org.example.banksystem.controller;

import org.example.banksystem.dto.CardRequest;
import org.example.banksystem.dto.CardResponse;
import org.example.banksystem.dto.UserRequest;
import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.User;
import org.example.banksystem.security.CommonsCodecHasher;
import org.example.banksystem.service.CardService;
import org.example.banksystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @Mock
    private CommonsCodecHasher codec;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getCards_ShouldReturnAllCards() {
        // Arrange
        Card card1 = new Card(1, "encrypted123", "user1", new Date(), "ACTIVE", 1000.0);
        Card card2 = new Card(2, "encrypted456", "user2", new Date(), "BLOCKED", 500.0);
        CardResponse cardResponse1 = new CardResponse(1, "****123", "USER1", new Date(), "ACTIVE");
        CardResponse cardResponse2 = new CardResponse(2, "****456", "USER2", new Date(), "BLOCKED");

        when(cardService.getAll()).thenReturn(Arrays.asList(card1, card2));
        when(cardService.parseCard(card1)).thenReturn(cardResponse1);
        when(cardService.parseCard(card2)).thenReturn(cardResponse2);

        // Act
        var result = adminController.getCards();

        // Assert
        assertTrue(result.getStatusCode().is2xxSuccessful());
        assertEquals(2, result.getBody().get("Cards").size());
        verify(cardService).getAll();
    }

    @Test
    void addCard_ShouldReturnSuccess_WhenValidCard() {
        // Arrange
        CardRequest cardRequest = new CardRequest("1234567890123456", "user1", new Date(), "ACTIVE", 1000.0);
        when(userService.exists("user1")).thenReturn(true);
        when(codec.encode("1234567890123456")).thenReturn("encrypted123");
        when(cardService.exists("encrypted123")).thenReturn(false);

        // Act
        var result = adminController.addCard(cardRequest);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Card added", result.getBody().get("Response"));
        verify(cardService).save(any(Card.class));
    }

    @Test
    void addCard_ShouldReturnBadRequest_WhenUserNotFound() {
        // Arrange
        CardRequest cardRequest = new CardRequest("1234567890123456", "unknown", new Date(), "ACTIVE", 1000.0);
        when(userService.exists("unknown")).thenReturn(false);

        // Act
        var result = adminController.addCard(cardRequest);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("User not found", result.getBody().get("Response"));
        verify(cardService, never()).save(any(Card.class));
    }

    @Test
    void addCard_ShouldReturnBadRequest_WhenCardExists() {
        // Arrange
        CardRequest cardRequest = new CardRequest("1234567890123456", "user1", new Date(), "ACTIVE", 1000.0);
        when(userService.exists("user1")).thenReturn(true);
        when(codec.encode("1234567890123456")).thenReturn("encrypted123");
        when(cardService.exists("encrypted123")).thenReturn(true);

        // Act
        var result = adminController.addCard(cardRequest);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("Card already exists", result.getBody().get("Response"));
        verify(cardService, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_ShouldReturnSuccess_WhenCardExists() {
        // Arrange
        when(cardService.exists(1)).thenReturn(true);

        // Act
        var result = adminController.deleteCard(1);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Card deleted", result.getBody().get("Response"));
        verify(cardService).delete(1);
    }

    @Test
    void deleteCard_ShouldReturnBadRequest_WhenCardNotExists() {
        // Arrange
        when(cardService.exists(999)).thenReturn(false);

        // Act
        var result = adminController.deleteCard(999);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("Card not found", result.getBody().get("Response"));
        verify(cardService, never()).delete(anyInt());
    }

    @Test
    void addUser_ShouldReturnSuccess_WhenNewUser() {
        // Arrange
        UserRequest userRequest = new UserRequest("newuser", "password", "ROLE_USER");
        when(userService.exists("newuser")).thenReturn(false);
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        var result = adminController.addUser(userRequest);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals("User added", result.getBody().get("Response"));
        verify(userService).save(any(User.class));
    }

    @Test
    void addUser_ShouldReturnBadRequest_WhenInvalidRole() {
        // Arrange
        UserRequest userRequest = new UserRequest("newuser", "password", "INVALID_ROLE");
        when(userService.exists("newuser")).thenReturn(false);

        // Act
        var result = adminController.addUser(userRequest);

        // Assert
        assertEquals(400, result.getStatusCode().value());
        assertEquals("Role not found", result.getBody().get("Response"));
        verify(userService, never()).save(any(User.class));
    }
}
