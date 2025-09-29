package org.example.banksystem.service;

import jakarta.transaction.Transactional;
import org.example.banksystem.dto.CardResponse;
import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.User;
import org.example.banksystem.repository.CardRepository;
import org.example.banksystem.security.CommonsCodecHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class CardService implements
        ServiceInterface<JpaRepository<Card, Integer>, Card, Integer> {

    @Autowired
    private CommonsCodecHasher coder;

    @Autowired
    private CardRepository cardRepository;

    @Override
    public JpaRepository<Card, Integer> getRepo() {
        return cardRepository;
    }

    public List<Card> getByUser(String username) {
        return cardRepository.findCardsByUsername(username).orElse(null);
    }

    public boolean exists(String cardNumber) {
        return cardRepository.existsByEncryptedCardNumber(cardNumber);
    }

    public CardResponse parseCard(Card card) {
        return new CardResponse(
                card.getCardId(),
                "*".repeat(12).concat(coder.decode(card.getEncryptedCardNumber()).substring(12)),
                card.getCardholder(),
                card.getExpiry_date(),
                card.getStatus()
        );
    }

    public boolean validateOwner(String username, Integer card_id) {
        return cardRepository.existsByCardholderAndCardId(username, card_id);
    }

    @Transactional
    public void blockCard(Integer card_id) {
        cardRepository.blockCard(card_id);
    }

    @Transactional
    public void transfer(Integer from, Integer to, Double amount) {
        cardRepository.decreaseCardBalance(from, amount);
        cardRepository.increaseCardBalance(to, amount);
    }
}
