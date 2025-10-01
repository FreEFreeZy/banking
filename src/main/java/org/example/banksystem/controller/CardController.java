package org.example.banksystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.banksystem.dto.request.*;
import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.dto.response.CardResponse;
import org.example.banksystem.entity.User;
import org.example.banksystem.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST контроллер для операций пользователя с банковскими картами
 * <p>
 * Предоставляет API для управления картами текущего аутентифицированного пользователя.
 * Все endpoints требуют аутентификации и доступны пользователям с ролями USER и ADMIN.
 * </p>
 *
 * @author George
 * @version 1.0
 * @see CardService
 */
@RequiredArgsConstructor
@RequestMapping("/api/card")
@RestController
@Tag(name = "Card Operations", description = "API для операций пользователя с банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    /**
     * Получает список всех карт текущего аутентифицированного пользователя
     *
     * @param user аутентифицированный пользователь
     * @return ResponseEntity с ApiResponseDTO содержащим список карт пользователя
     */
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
    public ResponseEntity<ApiResponseDTO<List<CardResponse>>> getCards(
            @Parameter(
                    description = "Аутентифицированный пользователь",
                    hidden = true
            )
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponseDTO.success("Cards:", cardService.getCardsByUsername(user.getUsername())));
    }

    /**
     * Блокирует карту пользователя по идентификатору
     *
     * @param card_id идентификатор карты для блокировки
     * @param user аутентифицированный пользователь
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> blockCard(
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
            @AuthenticationPrincipal User user) {
        cardService.blockCard(card_id, user.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.success("Card successfully blocked"));
    }

    /**
     * Выполняет перевод средств между картами текущего пользователя
     *
     * @param request DTO с данными для перевода
     * @param user аутентифицированный пользователь
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> transfer(
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
            @AuthenticationPrincipal User user) {
        cardService.transfer(request.from(), request.to(), request.amount(), user.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.success("Card successfully transferred"));
    }

}