package org.example.banksystem.service;

import org.example.banksystem.entity.Card;
import org.example.banksystem.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceInterfaceTest {

    @Mock
    private CardRepository repository;

    // Тестовая реализация интерфейса
    private final ServiceInterface<JpaRepository<Card, Integer>, Card, Integer> service =
            new ServiceInterface<>() {
                @Override
                public JpaRepository<Card, Integer> getRepo() {
                    return repository;
                }
            };

    @Test
    void get_ShouldReturnEntity() {
        // Arrange
        Integer id = 1;
        Card expectedCard = new Card();
        when(repository.getReferenceById(id)).thenReturn(expectedCard);

        // Act
        Card result = service.get(id);

        // Assert
        assertEquals(expectedCard, result);
        verify(repository).getReferenceById(id);
    }

    @Test
    void getAll_ShouldReturnAllEntities() {
        // Arrange
        List<Card> expectedCards = Arrays.asList(new Card(), new Card());
        when(repository.findAll()).thenReturn(expectedCards);

        // Act
        List<Card> result = service.getAll();

        // Assert
        assertEquals(expectedCards, result);
        verify(repository).findAll();
    }

    @Test
    void save_ShouldSaveEntity() {
        // Arrange
        Card card = new Card();

        // Act
        service.save(card);

        // Assert
        verify(repository).save(card);
    }

    @Test
    void delete_ShouldDeleteEntity() {
        // Arrange
        Integer id = 1;

        // Act
        service.delete(id);

        // Assert
        verify(repository).deleteById(id);
    }

    @Test
    void exists_ShouldReturnTrue_WhenEntityExists() {
        // Arrange
        Integer id = 1;
        when(repository.existsById(id)).thenReturn(true);

        // Act
        boolean result = service.exists(id);

        // Assert
        assertTrue(result);
        verify(repository).existsById(id);
    }

    @Test
    void exists_ShouldReturnFalse_WhenEntityNotExists() {
        // Arrange
        Integer id = 999;
        when(repository.existsById(id)).thenReturn(false);

        // Act
        boolean result = service.exists(id);

        // Assert
        assertFalse(result);
        verify(repository).existsById(id);
    }
}