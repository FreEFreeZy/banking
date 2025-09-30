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
@Schema(description = "DTO для запросов создания и обновления пользователей")
public class UserRequest {

    @Schema(
            description = "Имя пользователя",
            example = "user",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20
    )
    private String username;

    @Schema(
            description = "Пароль пользователя",
            example = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
            description = "Роль пользователя",
            example = "ROLE_USER",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"}
    )
    private String role;
}