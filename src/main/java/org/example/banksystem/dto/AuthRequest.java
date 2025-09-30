package org.example.banksystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для запросов аутентификации и регистрации")
public class AuthRequest {

    @Schema(
            description = "Имя пользователя",
            example = "ivan_petrov",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20
    )
    private String username;

    @Schema(
            description = "Пароль пользователя",
            example = "mySecurePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}