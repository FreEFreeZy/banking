package org.example.banksystem.controller;

import org.example.banksystem.dto.*;
import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.security.CommonsCodecHasher;
import org.example.banksystem.service.CardService;
import org.example.banksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/api/card")
@RestController
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonsCodecHasher codec;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/cards")
    private ResponseEntity<Map<String, List<CardResponse>>> getCards(Principal principal) {
        return ResponseEntity.ok(Map.of("Cards", cardService.getByUser(principal.getName()).stream()
                .map(card -> cardService.parseCard(card)).toList()));
    }

    @GetMapping("/cards/block/{card_id}")
    private ResponseEntity<Map<String, String>> blockCard(@PathVariable Integer card_id, Principal principal) {
        if (!cardService.validateOwner(principal.getName(), card_id)) {
            return ResponseEntity.status(403).body(Map.of("message", "Card does not belong to this account"));
        }
        cardService.blockCard(card_id);
        return ResponseEntity.ok(Map.of("message", "Card blocked"));
    }

    @PostMapping("/cards/transfer")
    private ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequest request, Principal principal) {
        if (!cardService.validateOwner(principal.getName(), request.getFrom())
        || !cardService.validateOwner(principal.getName(), request.getTo())) {
            return ResponseEntity.status(403).body(Map.of("message", "Card does not belong to this account"));
        }
        if (cardService.get(request.getFrom()).getBalance() < request.getAmount()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Not enough balance"));
        }
        cardService.transfer(request.getFrom(), request.getTo(), request.getAmount());
        return ResponseEntity.ok(Map.of("message", "Money transferred"));
    }

}
