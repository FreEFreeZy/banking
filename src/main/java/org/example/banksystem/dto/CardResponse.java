package org.example.banksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "DTO для ответа с данными банковской карты")
public class CardResponse {

    @Schema(
            description = "Уникальный идентификатор карты",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer card_id;

    @Schema(
            description = "Маскированный номер карты",
            example = "************1234",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String cardMask;

    @Schema(
            description = "Имя владельца карты(имя пользователя)",
            example = "user",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String cardHolder;

    @Schema(
            description = "Срок действия карты",
            example = "2025-12-31",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Date cardExp;

    @Schema(
            description = "Статус карты",
            example = "ACTIVE",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
    )
    private String status;
}