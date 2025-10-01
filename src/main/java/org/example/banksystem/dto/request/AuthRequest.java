package org.example.banksystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для запросов аутентификации и регистрации пользователя
 * <p>
 * Используется для передачи учетных данных при входе в систему и регистрации новых пользователей.
 * Содержит минимально необходимые поля для идентификации пользователя.
 * </p>
 *
 * @param username имя пользователя для аутентификации
 * @param password пароль пользователя для аутентификации
 */
@Schema(description = "DTO для запросов аутентификации и регистрации")
public record AuthRequest (
        @Schema(
                description = "Имя пользователя",
                example = "user",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 20)
        String username,

        @Schema(
                description = "Пароль пользователя",
                example = "mySecurePassword123",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {}