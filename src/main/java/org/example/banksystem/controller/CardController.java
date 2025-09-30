package org.example.banksystem.controller;

import org.example.banksystem.dto.*;
import org.example.banksystem.security.CommonsCodecHasher;
import org.example.banksystem.service.CardService;
import org.example.banksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/card")
@RestController
@Tag(name = "Card Operations", description = "API для операций пользователя с банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonsCodecHasher codec;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Operation(
            summary = "Получить карты пользователя",
            description = "Возвращает список всех карт текущего аутентифицированного пользователя"
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
            )
    })
    @GetMapping("/cards")
    public ResponseEntity<Map<String, List<CardResponse>>> getCards(
            @Parameter(
                    description = "Аутентифицированный пользователь",
                    hidden = true
            )
            Principal principal) {
        return ResponseEntity.ok(Map.of("Cards", cardService.getByUser(principal.getName()).stream()
                .map(card -> cardService.parseCard(card)).toList()));
    }

    @Operation(
            summary = "Заблокировать карту",
            description = "Блокировка карты по ID. Пользователь может блокировать только свои карты."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно заблокирована",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Карта не принадлежит пользователю",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @GetMapping("/cards/block/{card_id}")
    public ResponseEntity<Map<String, String>> blockCard(
            @Parameter(
                    description = "ID карты для блокировки",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer card_id,
            @Parameter(
                    description = "Аутентифицированный пользователь",
                    hidden = true
            )
            Principal principal) {
        if (!cardService.validateOwner(principal.getName(), card_id)) {
            return ResponseEntity.status(403).body(Map.of("message", "Card does not belong to this account"));
        }
        cardService.blockCard(card_id);
        return ResponseEntity.ok(Map.of("message", "Card blocked"));
    }

    @Operation(
            summary = "Перевод между картами",
            description = "Выполнение перевода средств между картами текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Перевод успешно выполнен",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации (карта заблокирована или недостаточно средств)",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Карта не принадлежит пользователю",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован"
            )
    })
    @PostMapping("/cards/transfer")
    public ResponseEntity<Map<String, String>> transfer(
            @Parameter(
                    description = "Данные для перевода",
                    required = true,
                    schema = @Schema(implementation = TransferRequest.class)
            )
            @RequestBody TransferRequest request,
            @Parameter(
                    description = "Аутентифицированный пользователь",
                    hidden = true
            )
            Principal principal) {
        if (!cardService.validateOwner(principal.getName(), request.getFrom())
                || !cardService.validateOwner(principal.getName(), request.getTo())) {
            return ResponseEntity.status(403).body(Map.of("message", "Card does not belong to this account"));
        }
        if (!cardService.get(request.getFrom()).getStatus().equals("BLOCKED")
                || !cardService.get(request.getTo()).getStatus().equals("BLOCKED")) {
            return ResponseEntity.status(400).body(Map.of("message", "Card is blocked"));
        }
        if (cardService.get(request.getFrom()).getBalance() < request.getAmount()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Not enough balance"));
        }
        cardService.transfer(request.getFrom(), request.getTo(), request.getAmount());
        return ResponseEntity.ok(Map.of("message", "Money transferred"));
    }

}