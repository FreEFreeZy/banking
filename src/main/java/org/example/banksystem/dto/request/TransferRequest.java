package org.example.banksystem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для запроса перевода средств между банковскими картами
 * <p>
 * Используется для передачи данных при выполнении операции перевода денежных средств
 * с одной карты на другую. Содержит информацию о картах участниках перевода и сумме операции.
 * </p>
 *
 * @param from идентификатор карты отправителя перевода
 * @param to идентификатор карты получателя перевода
 * @param amount сумма перевода между картами
 */
@Schema(description = "DTO для запроса перевода средств между картами")
public record TransferRequest(
        @Schema(
                description = "ID карты отправителя",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1")
        Integer from,

        @Schema(
                description = "ID карты получателя",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1")
        Integer to,

        @Schema(
                description = "Сумма перевода",
                example = "500.0",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0.01")
        Double amount
) {}