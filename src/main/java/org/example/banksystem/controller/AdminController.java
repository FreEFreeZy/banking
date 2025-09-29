package org.example.banksystem.controller;

import org.example.banksystem.dto.CardRequest;
import org.example.banksystem.dto.CardResponse;
import org.example.banksystem.dto.UserRequest;
import org.example.banksystem.dto.UserResponse;
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

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonsCodecHasher codec;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/cards")
    private ResponseEntity<Map<String, List<CardResponse>>> getCards() {
        return ResponseEntity.ok(Map.of("Cards",
                cardService.getAll().stream().map(card -> cardService.parseCard(card)).toList()));
    }

    @PostMapping("/cards")
    private ResponseEntity<Map<String, String>> addCard(@RequestBody CardRequest card) {
        if (!userService.exists(card.getCardholder())) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User not found"));
        }
        if (cardService.exists(codec.encode(card.getCard_number()))) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Card already exists"));
        }
        cardService.save(new Card(
                codec.encode(card.getCard_number()),
                card.getCardholder(),
                card.getExpiry_date(),
                card.getStatus()));
        return ResponseEntity.ok(Map.of("Response", "Card added"));
    }

    @PutMapping("/cards/{id}")
    private ResponseEntity<Map<String, String>> updateCard(@PathVariable("id") Integer id, @RequestBody CardRequest card) {
        if (!userService.exists(card.getCardholder())) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User not found"));
        }
        if (!cardService.exists(codec.encode(card.getCard_number()))) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Card not found"));
        }

        cardService.save(new Card(
                id,
                codec.encode(card.getCard_number()),
                card.getCardholder(),
                card.getExpiry_date(),
                card.getStatus()));
        return ResponseEntity.ok(Map.of("Response", "Card added"));
    }

    @DeleteMapping("/cards/{id}")
    private ResponseEntity<Map<String, String>> deleteCard(@PathVariable("id") Integer id) {
        if (!cardService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Card not found"));
        }
        cardService.delete(id);
        return ResponseEntity.ok(Map.of("Response", "Card deleted"));
    }



    @GetMapping("/users")
    private ResponseEntity<Map<String, List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(Map.of("Users", userService.getAll().stream().map(user -> userService.parseUser(user)).toList()));
    }

    @PostMapping("/users")
    private ResponseEntity<Map<String, String>> addUser(@RequestBody UserRequest user) {
        if (userService.exists(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User already exists"));
        }
        Optional<Role> optionalRole = Arrays.stream(Role.values())
                    .filter(role -> role.name().equals(user.getRole()))
                    .findFirst();
        if (optionalRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Role not found"));
        }
        userService.save(new User(
                user.getUsername(),
                bCryptPasswordEncoder.encode(user.getPassword()),
                optionalRole.get()));
        return ResponseEntity.ok(Map.of("Response", "User added"));
    }

    @PutMapping("/users/{id}")
    private ResponseEntity<Map<String, String>> updateUser(@PathVariable("id") String id, @RequestBody UserRequest user) {
        if (!userService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User not found"));
        }
        Optional<Role> optionalRole = Arrays.stream(Role.values())
                .filter(role -> role.name().equals(user.getRole()))
                .findFirst();
        if (optionalRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Role not found"));
        }
        userService.save(new User(
                id,
                bCryptPasswordEncoder.encode(user.getPassword()),
                optionalRole.get()));
        return ResponseEntity.ok(Map.of("Response", "User updated"));
    }

    @DeleteMapping("/users/{id}")
    private ResponseEntity<Map<String, String>> deleteUser(@PathVariable("id") String id) {
        if (!userService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User not found"));
        }
        userService.delete(id);
        return ResponseEntity.ok(Map.of("Response", "User deleted"));
    }


}
