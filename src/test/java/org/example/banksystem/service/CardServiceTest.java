package org.example.banksystem.service;

import org.example.banksystem.dto.CardResponse;
import org.example.banksystem.entity.Card;
import org.example.banksystem.repository.CardRepository;
import org.example.banksystem.security.CommonsCodecHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CommonsCodecHasher coder;

    @InjectMocks
    private CardService cardService;

    @Test
    void getRepo_ShouldReturnCardRepository() {
        // Act
        var result = cardService.getRepo();

        // Assert
        assertEquals(cardRepository, result);
    }

    @Test
    void getByUser_ShouldReturnUserCards_WhenUserExists() {
        // Arrange
        String username = "testuser";
        Card card1 = new Card(1, "encrypted123", username, new Date(), "ACTIVE", 1000.0);
        Card card2 = new Card(2, "encrypted456", username, new Date(), "ACTIVE", 500.0);
        List<Card> expectedCards = Arrays.asList(card1, card2);

        when(cardRepository.findCardsByUsername(username))
                .thenReturn(Optional.of(expectedCards));

        // Act
        List<Card> result = cardService.getByUser(username);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedCards, result);
        verify(cardRepository).findCardsByUsername(username);
    }

    @Test
    void getByUser_ShouldReturnNull_WhenUserHasNoCards() {
        // Arrange
        String username = "emptyuser";
        when(cardRepository.findCardsByUsername(username))
                .thenReturn(Optional.empty());

        // Act
        List<Card> result = cardService.getByUser(username);

        // Assert
        assertNull(result);
        verify(cardRepository).findCardsByUsername(username);
    }

    @Test
    void exists_ShouldReturnTrue_WhenCardNumberExists() {
        // Arrange
        String encryptedCardNumber = "encrypted123";
        when(cardRepository.existsByEncryptedCardNumber(encryptedCardNumber))
                .thenReturn(true);

        // Act
        boolean result = cardService.exists(encryptedCardNumber);

        // Assert
        assertTrue(result);
        verify(cardRepository).existsByEncryptedCardNumber(encryptedCardNumber);
    }

    @Test
    void exists_ShouldReturnFalse_WhenCardNumberNotExists() {
        // Arrange
        String encryptedCardNumber = "unknown123";
        when(cardRepository.existsByEncryptedCardNumber(encryptedCardNumber))
                .thenReturn(false);

        // Act
        boolean result = cardService.exists(encryptedCardNumber);

        // Assert
        assertFalse(result);
        verify(cardRepository).existsByEncryptedCardNumber(encryptedCardNumber);
    }

    @Test
    void parseCard_ShouldReturnCardResponse_WithMaskedNumber() {
        // Arrange
        Card card = new Card(1, "encrypted123456789012", "IVAN PETROV", new Date(), "ACTIVE", 1000.0);
        when(coder.decode("encrypted123456789012")).thenReturn("1234567890123456");

        // Act
        CardResponse result = cardService.parseCard(card);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCard_id());
        assertEquals("************3456", result.getCardMask());
        assertEquals("IVAN PETROV", result.getCardHolder());
        assertEquals("ACTIVE", result.getStatus());
        verify(coder).decode("encrypted123456789012");
    }

    @Test
    void validateOwner_ShouldReturnTrue_WhenUserOwnsCard() {
        // Arrange
        String username = "testuser";
        Integer cardId = 1;
        when(cardRepository.existsByCardholderAndCardId(username, cardId))
                .thenReturn(true);

        // Act
        boolean result = cardService.validateOwner(username, cardId);

        // Assert
        assertTrue(result);
        verify(cardRepository).existsByCardholderAndCardId(username, cardId);
    }

    @Test
    void validateOwner_ShouldReturnFalse_WhenUserNotOwnsCard() {
        // Arrange
        String username = "testuser";
        Integer cardId = 1;
        when(cardRepository.existsByCardholderAndCardId(username, cardId))
                .thenReturn(false);

        // Act
        boolean result = cardService.validateOwner(username, cardId);

        // Assert
        assertFalse(result);
        verify(cardRepository).existsByCardholderAndCardId(username, cardId);
    }

    @Test
    void blockCard_ShouldCallRepository() {
        // Arrange
        Integer cardId = 1;

        // Act
        cardService.blockCard(cardId);

        // Assert
        verify(cardRepository).blockCard(cardId);
    }

    @Test
    void transfer_ShouldCallRepositoryMethods() {
        // Arrange
        Integer fromCardId = 1;
        Integer toCardId = 2;
        Double amount = 100.0;

        // Act
        cardService.transfer(fromCardId, toCardId, amount);

        // Assert
        verify(cardRepository).decreaseCardBalance(fromCardId, amount);
        verify(cardRepository).increaseCardBalance(toCardId, amount);
    }

    @Test
    void get_ShouldCallRepositoryGetReferenceById() {
        // Arrange
        Integer cardId = 1;
        Card expectedCard = new Card();
        when(cardRepository.getReferenceById(cardId)).thenReturn(expectedCard);

        // Act
        Card result = cardService.get(cardId);

        // Assert
        assertEquals(expectedCard, result);
        verify(cardRepository).getReferenceById(cardId);
    }

    @Test
    void getAll_ShouldReturnAllCards() {
        // Arrange
        List<Card> expectedCards = Arrays.asList(new Card(), new Card());
        when(cardRepository.findAll()).thenReturn(expectedCards);

        // Act
        List<Card> result = cardService.getAll();

        // Assert
        assertEquals(expectedCards, result);
        verify(cardRepository).findAll();
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // Arrange
        Card card = new Card();

        // Act
        cardService.save(card);

        // Assert
        verify(cardRepository).save(card);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById() {
        // Arrange
        Integer cardId = 1;

        // Act
        cardService.delete(cardId);

        // Assert
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void existsById_ShouldCallRepositoryExistsById() {
        // Arrange
        Integer cardId = 1;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        // Act
        boolean result = cardService.exists(cardId);

        // Assert
        assertTrue(result);
        verify(cardRepository).existsById(cardId);
    }
}