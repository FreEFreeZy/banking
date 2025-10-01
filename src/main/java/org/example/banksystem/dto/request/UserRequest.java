package org.example.banksystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для запросов создания и обновления пользователей в системе
 * <p>
 * Используется для передачи данных при регистрации новых пользователей и обновлении
 * данных существующих пользователей. Содержит учетные данные и права доступа пользователя.
 * </p>
 *
 * @param username уникальное имя пользователя для идентификации в системе
 * @param password пароль пользователя для аутентификации
 * @param role роль пользователя в системе, определяющая уровень доступа
 */
@Schema(description = "DTO для запросов создания и обновления пользователей")
public record UserRequest (

        @Schema(
                description = "Имя пользователя",
                example = "user",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 20
        )
        String username,

        @Schema(
                description = "Пароль пользователя",
                example = "password",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Schema(
                description = "Роль пользователя",
                example = "ROLE_USER",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
        String role
) {}