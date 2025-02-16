package ru.nsu.fit.mihanizzm.litecrm.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Transaction response")
public record TransactionResponseDto(
        @Schema(description = "Transaction's ID represented by an integer value", example = "148")
        Integer id,

        @Schema(description = "Seller ID of a transaction initiator. Cannot be null.", example = "8")
        Integer sellerId,

        @Schema(
                description = "Amount of transaction represented by BigDecimal value. Must be positive.",
                example = "6.9"
        )
        BigDecimal amount,

        @Schema(
                description = "Transaction's payment type. Represented by enum.",
                allowableValues = {"CARD", "TRANSFER", "CASH"},
                example = "CARD"
        )
        String paymentType,

        @Schema(
                description = "The date when transaction was processed. Generated automatically at the sever side.",
                example = "2025-01-01T00:00:00.000Z"
        )
        LocalDateTime transactionDate
) {}
