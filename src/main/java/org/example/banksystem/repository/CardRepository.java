package org.example.banksystem.repository;

import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {

    @Query("SELECT c.cardId, c.encryptedCardNumber, c.cardholder, c.expiry_date, c.status " +
            "FROM Card c WHERE c.cardholder = :username")
    Optional<List<Card>> findCardsByUsername(@Param("username") String username);

    boolean existsByEncryptedCardNumber(String encrypted_card_number);

    boolean existsByCardholderAndCardId(String cardholder, Integer cardId);

    @Modifying
    @Query("UPDATE Card c SET c.status = 'BLOCKED' WHERE c.cardId = :cardId")
    void blockCard(@Param("cardId") Integer cardId);

    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance - :amount WHERE c.cardId = :cardId")
    void decreaseCardBalance(@Param("cardId") Integer cardId, @Param("amount") Double amount);

    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance + :amount WHERE c.cardId = :cardId")
    void increaseCardBalance(@Param("cardId") Integer cardId, @Param("amount") Double amount);
}
