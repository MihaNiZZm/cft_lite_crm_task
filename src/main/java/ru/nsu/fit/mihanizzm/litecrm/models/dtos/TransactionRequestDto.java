package ru.nsu.fit.mihanizzm.litecrm.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Transaction creating or updating request")
public record TransactionRequestDto (
        @Schema(description = "Seller ID of a transaction initiator. Cannot be null.", example = "420")
        Integer sellerId,

        @Schema(
                description = "Amount of transaction represented by BigDecimal value. Must be positive.",
                example = "22.8"
        )
        BigDecimal amount,

        @Schema(
                description = "Transaction's payment type. Represented by enum.",
                allowableValues = {"CARD", "TRANSFER", "CASH"},
                example = "CARD"
        )
        String paymentType
) {}
