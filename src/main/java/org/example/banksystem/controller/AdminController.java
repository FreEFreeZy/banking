package org.example.banksystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.banksystem.dto.request.*;
import org.example.banksystem.dto.response.*;
import org.example.banksystem.service.CardService;
import org.example.banksystem.service.UserService;
import org.springframework.http.ResponseEntity;
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

/**
 * REST контроллер для административного управления пользователями и банковскими картами
 * <p>
 * Предоставляет API для выполнения CRUD операций с пользователями и картами.
 * Все endpoints требуют аутентификации с ролью ADMIN.
 * </p>
 *
 * @author George
 * @version 1.0
 * @see CardService
 * @see UserService
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "API для административного управления пользователями и картами")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    /**
     * Получает список всех банковских карт в системе
     *
     * @return ResponseEntity с ApiResponseDTO содержащим список карт
     */
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
    public ResponseEntity<ApiResponseDTO<List<CardResponse>>> getCards() {
        return ResponseEntity.ok(ApiResponseDTO.success("Cards", cardService.getAllCards()));
    }

    /**
     * Создает новую банковскую карту для пользователя
     *
     * @param card DTO с данными для создания карты
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> addCard(
            @Parameter(
                    description = "Данные для создания карты",
                    required = true,
                    schema = @Schema(implementation = CardRequest.class)
            )
            @RequestBody CardRequest card) {
        cardService.addCard(card.card_number(), card.cardholder(), card.expiry_date());
        return ResponseEntity.ok(ApiResponseDTO.success("Card added"));
    }

    /**
     * Обновляет данные существующей банковской карты
     *
     * @param card DTO с новыми данными карты
     * @return ResponseEntity с результатом операции
     */
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
    @PutMapping("/cards/")
    public ResponseEntity<ApiResponseDTO<Void>> updateCard(
            @Parameter(
                    description = "Новые данные карты",
                    required = true,
                    schema = @Schema(implementation = CardRequest.class)
            )
            @RequestBody CardRequest card) {
        cardService.updateCard(card.cardId(), card.card_number(), card.cardholder(), card.expiry_date(), card.status(), card.balance());
        return ResponseEntity.ok(ApiResponseDTO.success("Card updated"));
    }

    /**
     * Удаляет банковскую карту по идентификатору
     *
     * @param id идентификатор карты для удаления
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> deleteCard(
            @Parameter(
                    description = "ID карты для удаления",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Integer id) {
        cardService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Card successfully deleted"));
    }

    /**
     * Получает список всех пользователей системы
     *
     * @return ResponseEntity с ApiResponseDTO содержащим список пользователей
     */
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
    public ResponseEntity<ApiResponseDTO<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponseDTO.success("Users", userService.getAllUsers()));
    }

    /**
     * Создает нового пользователя в системе
     *
     * @param user DTO с данными для создания пользователя
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> addUser(
            @Parameter(
                    description = "Данные для создания пользователя",
                    required = true,
                    schema = @Schema(implementation = UserRequest.class)
            )
            @RequestBody UserRequest user) {
        userService.addUser(user.username(), user.password(), user.role());
        return ResponseEntity.ok(ApiResponseDTO.success("User successfully added"));
    }

    /**
     * Обновляет данные существующего пользователя
     *
     * @param user DTO с новыми данными пользователя
     * @return ResponseEntity с результатом операции
     */
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
    @PutMapping("/users/")
    public ResponseEntity<ApiResponseDTO<Void>> updateUser(
            @Parameter(
                    description = "Новые данные пользователя",
                    required = true,
                    schema = @Schema(implementation = UserRequest.class)
            )
            @RequestBody UserRequest user) {
        userService.updateUser(user.username(), user.password(), user.role());
        return ResponseEntity.ok(ApiResponseDTO.success("User successfully updated"));
    }

    /**
     * Удаляет пользователя по идентификатору
     *
     * @param id идентификатор пользователя для удаления
     * @return ResponseEntity с результатом операции
     */
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
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(
            @Parameter(
                    description = "ID пользователя для удаления",
                    required = true,
                    example = "user123"
            )
            @PathVariable("id") String id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User successfully deleted"));
    }
}