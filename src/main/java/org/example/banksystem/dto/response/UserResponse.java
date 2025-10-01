package org.example.banksystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа с данными пользователя системы
 * <p>
 * Используется для возврата информации о пользователе в API ответах.
 * Содержит основные данные пользователя, включая учетные данные и права доступа.
 * Все поля доступны только для чтения и не должны изменяться клиентом.
 * </p>
 *
 * @param username уникальное имя пользователя для идентификации в системе
 * @param encryptedPassword зашифрованный пароль пользователя (хеш)
 * @param role роль пользователя в системе, определяющая уровень доступа
 */
@Schema(description = "DTO для ответа с данными пользователя")
public record UserResponse(

        @Schema(
                description = "Имя пользователя",
                example = "user",
                accessMode = Schema.AccessMode.READ_ONLY)
        String username,

        @Schema(
                description = "Зашифрованный пароль пользователя",
                example = "$2a$10$xyz123...",
                accessMode = Schema.AccessMode.READ_ONLY)
        String encryptedPassword,

        @Schema(
                description = "Роль пользователя",
                example = "ROLE_USER",
                accessMode = Schema.AccessMode.READ_ONLY,
                allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
        String role
) {}