package org.example.banksystem.service;

import org.example.banksystem.dto.response.CardResponse;
import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.CardStatus;
import org.example.banksystem.exceptions.cards.CardAccessDeniedException;
import org.example.banksystem.exceptions.cards.CardNotFoundException;
import org.example.banksystem.exceptions.cards.CardNotInService;
import org.example.banksystem.exceptions.cards.CardWrongCredentials;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.repository.CardRepository;
import org.example.banksystem.repository.UserRepository;
import org.example.banksystem.security.CommonsCodecHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса банковских карт CardService
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CommonsCodecHasher coder;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private final Integer CARD_ID = 1;
    private final String USERNAME = "testuser";
    private final String CARD_NUMBER = "1234567812345678";
    private final String ENCRYPTED_CARD_NUMBER = "encrypted123";
    private final Date EXPIRY_DATE = new Date();

    @BeforeEach
    void setUp() {
        testCard = new Card(CARD_ID, ENCRYPTED_CARD_NUMBER, USERNAME, EXPIRY_DATE, CardStatus.ACTIVE, 1000.0);
    }

    @Test
    void parseCard_ShouldReturnCardResponseWithMaskedNumber() {
        // Arrange
        String decodedCardNumber = "1234567812345678";
        String maskedNumber = "************5678";

        when(coder.decode(ENCRYPTED_CARD_NUMBER)).thenReturn(decodedCardNumber);

        // Act
        CardResponse result = cardService.parseCard(testCard);

        // Assert
        assertNotNull(result);
        assertEquals(CARD_ID, result.card_id());
        assertEquals(maskedNumber, result.cardMask());
        assertEquals(USERNAME, result.cardHolder());
        assertEquals(EXPIRY_DATE, result.cardExp());
        assertEquals("ACTIVE", result.status());
        verify(coder).decode(ENCRYPTED_CARD_NUMBER);
    }

    @Test
    void validateOwner_WhenCardBelongsToUser_ShouldReturnTrue() {
        // Arrange
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(true);

        // Act
        boolean result = cardService.validateOwner(USERNAME, CARD_ID);

        // Assert
        assertTrue(result);
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
    }

    @Test
    void validateOwner_WhenCardNotBelongsToUser_ShouldReturnFalse() {
        // Arrange
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(false);

        // Act
        boolean result = cardService.validateOwner(USERNAME, CARD_ID);

        // Assert
        assertFalse(result);
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
    }

    @Test
    void blockCard_WithValidData_ShouldBlockCard() {
        // Arrange
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));

        // Act
        cardService.blockCard(CARD_ID, USERNAME);

        // Assert
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
        verify(cardRepository).findById(CARD_ID);
        verify(cardRepository).blockCard(CARD_ID);
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
    }

    @Test
    void blockCard_WhenUserNotOwner_ShouldThrowCardAccessDeniedException() {
        // Arrange
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(false);

        // Act & Assert
        CardAccessDeniedException exception = assertThrows(CardAccessDeniedException.class,
                () -> cardService.blockCard(CARD_ID, USERNAME));

        assertEquals("Access denied", exception.getMessage());
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
        verify(cardRepository, never()).findById(anyInt());
        verify(cardRepository, never()).blockCard(anyInt());
    }

    @Test
    void blockCard_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        // Arrange
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.blockCard(CARD_ID, USERNAME));

        assertEquals("Card not found", exception.getMessage());
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
        verify(cardRepository).findById(CARD_ID);
        verify(cardRepository, never()).blockCard(anyInt());
    }

    @Test
    void blockCard_WhenCardNotActive_ShouldThrowCardNotInService() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.existsByCardholderAndCardId(USERNAME, CARD_ID)).thenReturn(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(testCard));

        // Act & Assert
        CardNotInService exception = assertThrows(CardNotInService.class,
                () -> cardService.blockCard(CARD_ID, USERNAME));

        assertEquals("Card not in active status", exception.getMessage());
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, CARD_ID);
        verify(cardRepository).findById(CARD_ID);
        verify(cardRepository, never()).blockCard(anyInt());
    }

    @Test
    void transfer_WithValidData_ShouldTransferFunds() {
        // Arrange
        Integer fromCardId = 1;
        Integer toCardId = 2;
        Double amount = 100.0;

        when(cardRepository.existsByCardholderAndCardId(USERNAME, fromCardId)).thenReturn(true);
        when(cardRepository.existsByCardholderAndCardId(USERNAME, toCardId)).thenReturn(true);

        // Act
        cardService.transfer(fromCardId, toCardId, amount, USERNAME);

        // Assert
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, fromCardId);
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, toCardId);
        verify(cardRepository).decreaseCardBalance(fromCardId, amount);
        verify(cardRepository).increaseCardBalance(toCardId, amount);
    }

    @Test
    void transfer_WhenUserNotOwner_ShouldThrowCardAccessDeniedException() {
        // Arrange
        Integer fromCardId = 1;
        Integer toCardId = 2;
        Double amount = 100.0;

        when(cardRepository.existsByCardholderAndCardId(USERNAME, fromCardId)).thenReturn(false);

        // Act & Assert
        CardAccessDeniedException exception = assertThrows(CardAccessDeniedException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount, USERNAME));

        assertEquals("Access denied", exception.getMessage());
        verify(cardRepository).existsByCardholderAndCardId(USERNAME, fromCardId);
        verify(cardRepository, never()).existsByCardholderAndCardId(USERNAME, toCardId);
        verify(cardRepository, never()).decreaseCardBalance(anyInt(), anyDouble());
        verify(cardRepository, never()).increaseCardBalance(anyInt(), anyDouble());
    }

    @Test
    void getCardsByUsername_WhenCardsExist_ShouldReturnCardResponses() {
        // Arrange
        List<Card> cards = List.of(testCard);
        when(cardRepository.findCardsByUsername(USERNAME)).thenReturn(Optional.of(cards));
        when(coder.decode(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);

        // Act
        List<CardResponse> result = cardService.getCardsByUsername(USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findCardsByUsername(USERNAME);
        verify(coder).decode(ENCRYPTED_CARD_NUMBER);
    }

    @Test
    void getCardsByUsername_WhenNoCards_ShouldReturnEmptyList() {
        // Arrange
        when(cardRepository.findCardsByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act
        List<CardResponse> result = cardService.getCardsByUsername(USERNAME);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository).findCardsByUsername(USERNAME);
        verify(coder, never()).decode(anyString());
    }

    @Test
    void getAllCards_ShouldReturnAllCardResponses() {
        // Arrange
        List<Card> cards = List.of(testCard);
        when(cardRepository.findAll()).thenReturn(cards);
        when(coder.decode(ENCRYPTED_CARD_NUMBER)).thenReturn(CARD_NUMBER);

        // Act
        List<CardResponse> result = cardService.getAllCards();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findAll();
        verify(coder).decode(ENCRYPTED_CARD_NUMBER);
    }

    @Test
    void addCard_WithValidData_ShouldSaveCard() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(coder.decode(CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(cardRepository.existsByEncryptedCardNumber(CARD_NUMBER)).thenReturn(false);
        when(coder.encode(CARD_NUMBER)).thenReturn(ENCRYPTED_CARD_NUMBER);

        // Act
        cardService.addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE);

        // Assert
        verify(userRepository).existsById(USERNAME);
        verify(coder).decode(CARD_NUMBER);
        verify(cardRepository).existsByEncryptedCardNumber(CARD_NUMBER);
        verify(coder).encode(CARD_NUMBER);
        verify(cardRepository).save(argThat(card ->
                card.getEncryptedCardNumber().equals(ENCRYPTED_CARD_NUMBER) &&
                        card.getCardholder().equals(USERNAME) &&
                        card.getExpiry_date().equals(EXPIRY_DATE) &&
                        card.getStatus() == CardStatus.ACTIVE &&
                        card.getBalance() == 0.0
        ));
    }

    @Test
    void addCard_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> cardService.addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(coder, never()).decode(anyString());
        verify(cardRepository, never()).existsByEncryptedCardNumber(anyString());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void addCard_WhenCardNumberExists_ShouldThrowCardWrongCredentials() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(coder.decode(CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(cardRepository.existsByEncryptedCardNumber(CARD_NUMBER)).thenReturn(true);

        // Act & Assert
        CardWrongCredentials exception = assertThrows(CardWrongCredentials.class,
                () -> cardService.addCard(CARD_NUMBER, USERNAME, EXPIRY_DATE));

        assertEquals("Card number already taken", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(coder).decode(CARD_NUMBER);
        verify(cardRepository).existsByEncryptedCardNumber(CARD_NUMBER);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCard_WithValidData_ShouldUpdateCard() {
        // Arrange
        String status = "ACTIVE";
        Double amount = 1500.0;

        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(coder.decode(CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(cardRepository.existsByEncryptedCardNumber(CARD_NUMBER)).thenReturn(true);
        when(coder.encode(CARD_NUMBER)).thenReturn(ENCRYPTED_CARD_NUMBER);

        // Act
        cardService.updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, status, amount);

        // Assert
        verify(userRepository).existsById(USERNAME);
        verify(coder).decode(CARD_NUMBER);
        verify(cardRepository).existsByEncryptedCardNumber(CARD_NUMBER);
        verify(coder).encode(CARD_NUMBER);
        verify(cardRepository).save(argThat(card ->
                card.getCardId().equals(CARD_ID) &&
                        card.getEncryptedCardNumber().equals(ENCRYPTED_CARD_NUMBER) &&
                        card.getCardholder().equals(USERNAME) &&
                        card.getExpiry_date().equals(EXPIRY_DATE) &&
                        card.getStatus() == CardStatus.ACTIVE &&
                        card.getBalance().equals(amount)
        ));
    }

    @Test
    void updateCard_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> cardService.updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "ACTIVE", 1000.0));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(coder, never()).decode(anyString());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCard_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(coder.decode(CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(cardRepository.existsByEncryptedCardNumber(CARD_NUMBER)).thenReturn(false);

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "ACTIVE", 1000.0));

        assertEquals("Card not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(coder).decode(CARD_NUMBER);
        verify(cardRepository).existsByEncryptedCardNumber(CARD_NUMBER);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void updateCard_WhenInvalidStatus_ShouldThrowCardWrongCredentials() {
        // Arrange
        when(userRepository.existsById(USERNAME)).thenReturn(true);
        when(coder.decode(CARD_NUMBER)).thenReturn(CARD_NUMBER);
        when(cardRepository.existsByEncryptedCardNumber(CARD_NUMBER)).thenReturn(true);

        // Act & Assert
        CardWrongCredentials exception = assertThrows(CardWrongCredentials.class,
                () -> cardService.updateCard(CARD_ID, CARD_NUMBER, USERNAME, EXPIRY_DATE, "INVALID_STATUS", 1000.0));

        assertEquals("Status not found", exception.getMessage());
        verify(userRepository).existsById(USERNAME);
        verify(coder).decode(CARD_NUMBER);
        verify(cardRepository).existsByEncryptedCardNumber(CARD_NUMBER);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void delete_WhenCardExists_ShouldDeleteCard() {
        // Arrange
        when(cardRepository.existsById(CARD_ID)).thenReturn(true);

        // Act
        cardService.delete(CARD_ID);

        // Assert
        verify(cardRepository).existsById(CARD_ID);
        verify(cardRepository).deleteById(CARD_ID);
    }

    @Test
    void delete_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        // Arrange
        when(cardRepository.existsById(CARD_ID)).thenReturn(false);

        // Act & Assert
        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.delete(CARD_ID));

        assertEquals("Card not found", exception.getMessage());
        verify(cardRepository).existsById(CARD_ID);
        verify(cardRepository, never()).deleteById(anyInt());
    }
}