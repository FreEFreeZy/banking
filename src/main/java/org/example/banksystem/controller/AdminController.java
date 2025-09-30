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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "API для административного управления пользователями и картами")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonsCodecHasher codec;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Operation(
            summary = "Получить все карты",
            description = "Возвращает список всех банковских карт в системе. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка карт",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @GetMapping("/cards")
    public ResponseEntity<Map<String, List<CardResponse>>> getCards() {
        return ResponseEntity.ok(Map.of("Cards",
                cardService.getAll().stream().map(card -> cardService.parseCard(card)).toList()));
    }

    @Operation(
            summary = "Добавить новую карту",
            description = "Создание новой банковской карты для пользователя. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно добавлена",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса (пользователь не найден или карта уже существует)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @PostMapping("/cards")
    public ResponseEntity<Map<String, String>> addCard(
            @Parameter(
                    description = "Данные для создания карты",
                    required = true,
                    schema = @Schema(implementation = CardRequest.class)
            )
            @RequestBody CardRequest card) {
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
                card.getStatus(),
                card.getBalance()));
        return ResponseEntity.ok(Map.of("Response", "Card added"));
    }

    @Operation(
            summary = "Обновить карту",
            description = "Обновление данных банковской карты по ID. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно обновлена",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса (пользователь не найден или карта не существует)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @PutMapping("/cards/{id}")
    public ResponseEntity<Map<String, String>> updateCard(
            @Parameter(
                    description = "ID карты для обновления",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Integer id,
            @Parameter(
                    description = "Новые данные карты",
                    required = true,
                    schema = @Schema(implementation = CardRequest.class)
            )
            @RequestBody CardRequest card) {
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
                card.getStatus(),
                card.getBalance()));
        return ResponseEntity.ok(Map.of("Response", "Card added"));
    }

    @Operation(
            summary = "Удалить карту",
            description = "Удаление банковской карты по ID. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно удалена",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Карта с указанным ID не найдена"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Map<String, String>> deleteCard(
            @Parameter(
                    description = "ID карты для удаления",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Integer id) {
        if (!cardService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("Response", "Card not found"));
        }
        cardService.delete(id);
        return ResponseEntity.ok(Map.of("Response", "Card deleted"));
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей системы. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка пользователей",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @GetMapping("/users")
    public ResponseEntity<Map<String, List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(Map.of("Users", userService.getAll().stream().map(user -> userService.parseUser(user)).toList()));
    }

    @Operation(
            summary = "Добавить нового пользователя",
            description = "Создание нового пользователя в системе. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно добавлен",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса (пользователь уже существует или роль не найдена)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> addUser(
            @Parameter(
                    description = "Данные для создания пользователя",
                    required = true,
                    schema = @Schema(implementation = UserRequest.class)
            )
            @RequestBody UserRequest user) {
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

    @Operation(
            summary = "Обновить пользователя",
            description = "Обновление данных пользователя по ID. Только для администраторов."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса (пользователь не найден или роль не найдена)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @Parameter(
                    description = "ID пользователя для обновления",
                    required = true,
                    example = "user123"
            )
            @PathVariable("id") String id,
            @Parameter(
                    description = "Новые данные пользователя",
                    required = true,
                    schema = @Schema(implementation = UserRequest.class)
            )
            @RequestBody UserRequest user) {
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

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаление пользователя по ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно удален",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Пользователь с указанным ID не найден"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав (требуется роль ADMIN)"
            )
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(
                    description = "ID пользователя для удаления",
                    required = true,
                    example = "user123"
            )
            @PathVariable("id") String id) {
        if (!userService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("Response", "User not found"));
        }
        userService.delete(id);
        return ResponseEntity.ok(Map.of("Response", "User deleted"));
    }
}