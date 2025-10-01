package org.example.banksystem.controller;

import lombok.RequiredArgsConstructor;
import org.example.banksystem.dto.request.AuthRequest;
import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер для обработки запросов аутентификации и регистрации пользователей.
 * Предоставляет endpoints для входа в систему и создания новых учетных записей.
 *
 * @author George
 * @version 1.0
 * @see AuthService
 */
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
@Tag(name = "Authentication", description = "API для аутентификации и регистрации пользователей")
public class AuthController {

    private final AuthService authService;

    /**
     * Выполняет аутентификацию пользователя в системе.
     * При успешной аутентификации возвращает JWT токен в виде HTTP cookie.
     *
     * @param request объект {@link AuthRequest} содержащий учетные данные пользователя
     * @return {@link ResponseEntity} с {@link ApiResponseDTO} и JWT токеном в cookie
     * @see AuthRequest
     * @see ApiResponseDTO
     */
    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя и получение JWT токена в cookie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный пароль",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<Void>> login(
            @Parameter(
                    description = "Данные для аутентификации",
                    required = true,
                    schema = @Schema(implementation = AuthRequest.class)
            )
            @RequestBody AuthRequest request) {
        ResponseCookie cookie = authService.login(request.username(), request.password());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponseDTO.success("Authorization success"));
    }

    /**
     * Регистрирует нового пользователя в системе.
     * Создает учетную запись с ролью ROLE_USER по умолчанию.
     *
     * @param request объект {@link AuthRequest} содержащий данные для регистрации
     * @return {@link ResponseEntity} с {@link ApiResponseDTO} содержащим результат операции
     * @see AuthRequest
     * @see ApiResponseDTO
     */
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создание нового пользователя с ролью ROLE_USER"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь уже существует",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Void>> register(
            @Parameter(
                    description = "Данные для регистрации",
                    required = true,
                    schema = @Schema(implementation = AuthRequest.class)
            )
            @RequestBody AuthRequest request) {
        authService.registerUser(request.username(), request.password());
        return ResponseEntity.ok(ApiResponseDTO.success("User successfully registered"));
    }
}