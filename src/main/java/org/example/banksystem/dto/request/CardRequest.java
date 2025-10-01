package org.example.banksystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * DTO для запросов создания и обновления банковских карт
 * <p>
 * Используется для передачи данных при создании новых карт и обновлении существующих.
 * Содержит полную информацию о банковской карте, включая идентификационные данные и финансовую информацию.
 * </p>
 *
 * @param cardId идентификатор банковской карты
 * @param card_number номер банковской карты
 * @param cardholder имя владельца карты
 * @param expiry_date срок действия карты
 * @param status текущий статус карты
 * @param balance текущий баланс карты
 */
@Schema(description = "DTO для запросов создания и обновления банковских карт")
public record CardRequest(
        @Schema(
                description = "ID банковской карты",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer cardId,

        @Schema(
                description = "Номер банковской карты",
                example = "1234567812345678",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 16,
                maxLength = 16)
        String card_number,

        @Schema(
                description = "Имя владельца карты (ник пользователя)",
                example = "user",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 20)
        String cardholder,

        @Schema(
                description = "Срок действия карты",
                example = "2025-12-31",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Date expiry_date,

        @Schema(
                description = "Статус карты",
                example = "ACTIVE",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"})
        String status,

        @Schema(
                description = "Баланс карты",
                example = "1500.75",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0")
        Double balance
) {}