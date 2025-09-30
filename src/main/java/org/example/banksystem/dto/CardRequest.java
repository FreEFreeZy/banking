package org.example.banksystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для запросов создания и обновления банковских карт")
public class CardRequest {

    @Schema(
            description = "Номер банковской карты",
            example = "1234567812345678",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 16,
            maxLength = 16
    )
    private String card_number;

    @Schema(
            description = "Имя владельца карты (ник пользователя)",
            example = "user",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20
    )
    private String cardholder;

    @Schema(
            description = "Срок действия карты",
            example = "2025-12-31",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Date expiry_date;

    @Schema(
            description = "Статус карты",
            example = "ACTIVE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
    )
    private String status;

    @Schema(
            description = "Баланс карты",
            example = "1500.75",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0"
    )
    private Double balance;
}