package org.example.banksystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Стандартный DTO для унифицированных ответов API
 * <p>
 * Предоставляет единый формат для всех ответов API, включая успешные операции и ошибки.
 * Содержит статус выполнения, сообщение для клиента, данные ответа и временную метку.
 * </p>
 *
 * @param status статус выполнения операции (success/error)
 * @param message текстовое сообщение с результатом операции
 * @param data данные ответа (может быть null для операций без возвращаемых данных)
 * @param timestamp временная метка формирования ответа
 * @param <T> тип данных возвращаемых в ответе
 */
@Schema(description = "Стандартный ответ API")
public record ApiResponseDTO<T>(
        @Schema(description = "Статус операции", example = "success", requiredMode = REQUIRED)
        String status,

        @Schema(description = "Сообщение для пользователя", example = "Operation completed successfully")
        String message,

        @Schema(description = "Данные ответа")
        T data,

        @Schema(description = "Временная метка ответа", example = "2024-01-15T10:30:00.123Z")
        LocalDateTime timestamp
) {

    /**
     * Создает успешный ответ API с данными
     *
     * @param message сообщение об успешном выполнении операции
     * @param data данные для возврата клиенту
     * @return ApiResponseDTO с статусом "success" и переданными данными
     * @param <T> тип возвращаемых данных
     */
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>("success", message, data, LocalDateTime.now());
    }

    /**
     * Создает успешный ответ API без данных
     *
     * @param message сообщение об успешном выполнении операции
     * @return ApiResponseDTO с статусом "success" и null данными
     */
    public static ApiResponseDTO<Void> success(String message) {
        return new ApiResponseDTO<>("success", message, null, LocalDateTime.now());
    }

    /**
     * Создает ответ с ошибкой без дополнительных данных
     *
     * @param message сообщение об ошибке
     * @return ApiResponseDTO с статусом "error" и null данными
     * @param <T> тип возвращаемых данных
     */
    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>("error", message, null, LocalDateTime.now());
    }

    /**
     * Создает ответ с ошибкой с дополнительными данными
     *
     * @param message сообщение об ошибке
     * @param details дополнительные данные об ошибке
     * @return ApiResponseDTO с статусом "error" и деталями ошибки
     * @param <T> тип данных с деталями ошибки
     */
    public static <T> ApiResponseDTO<T> error(String message, T details) {
        return new ApiResponseDTO<>("error", message, details, LocalDateTime.now());
    }
}