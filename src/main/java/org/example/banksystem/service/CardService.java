package org.example.banksystem.service;

import org.example.banksystem.entity.Card;
import org.example.banksystem.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {
    @Autowired
    private CardRepository cardRepository;

    public Card get(String card_number) {
        return cardRepository.getReferenceById(card_number);
    }

    public List<Card> getAll() {
        return cardRepository.findAll();
    }

    public void save(Card card) {
        cardRepository.save(card);
    }

    public void delete(String card_number) {
        cardRepository.deleteById(card_number);
    }
}
