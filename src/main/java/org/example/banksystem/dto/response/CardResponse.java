package org.example.banksystem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * DTO для ответа с данными банковской карты
 * <p>
 * Используется для возврата информации о банковской карте в API ответах.
 * Содержит основные данные карты в безопасном формате (с маскированным номером).
 * Все поля доступны только для чтения.
 * </p>
 *
 * @param card_id уникальный идентификатор карты в системе
 * @param cardMask маскированный номер карты для безопасного отображения
 * @param cardHolder имя владельца карты
 * @param cardExp срок действия карты
 * @param status текущий статус карты
 */
@Schema(description = "DTO для ответа с данными банковской карты")
public record CardResponse(
        @Schema(
                description = "Уникальный идентификатор карты",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer card_id,

        @Schema(
                description = "Маскированный номер карты",
                example = "************1234",
                accessMode = Schema.AccessMode.READ_ONLY)
        String cardMask,

        @Schema(
                description = "Имя владельца карты(имя пользователя)",
                example = "user",
                accessMode = Schema.AccessMode.READ_ONLY)
        String cardHolder,

        @Schema(
                description = "Срок действия карты",
                example = "2025-12-31",
                accessMode = Schema.AccessMode.READ_ONLY)
        Date cardExp,

        @Schema(
                description = "Статус карты",
                example = "ACTIVE",
                accessMode = Schema.AccessMode.READ_ONLY,
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"})
        String status
) {}