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
@Schema(description = "DTO для запроса перевода средств между картами")
public class TransferRequest {

    @Schema(
            description = "ID карты отправителя",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer from;

    @Schema(
            description = "ID карты получателя",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer to;

    @Schema(
            description = "Сумма перевода",
            example = "500.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    private Double amount;
}