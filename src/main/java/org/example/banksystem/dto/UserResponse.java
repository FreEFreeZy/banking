package org.example.banksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для ответа с данными пользователя")
public class UserResponse {

    @Schema(
            description = "Имя пользователя",
            example = "user",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String username;

    @Schema(
            description = "Зашифрованный пароль пользователя",
            example = "$2a$10$xyz123...",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String encryptedPassword;

    @Schema(
            description = "Роль пользователя",
            example = "ROLE_USER",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"}
    )
    private String role;
}