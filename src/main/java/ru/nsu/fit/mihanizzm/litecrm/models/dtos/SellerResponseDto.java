package ru.nsu.fit.mihanizzm.litecrm.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Seller response")
public record SellerResponseDto(
        @Schema(description = "Seller's ID represented by an integer value", example = "1337")
        Integer id,

        @Schema(description = "Sellers name", example = "John")
        String name,

        @Schema(description = "Contact info of a seller", example = "john17@gmail.com")
        String contactInfo,

        @Schema(
                description = "Registration date of a seller. Generated automatically at the sever side.",
                example = "2025-01-01T00:00:00.000Z"
        )
        LocalDateTime registrationDate
) {}
