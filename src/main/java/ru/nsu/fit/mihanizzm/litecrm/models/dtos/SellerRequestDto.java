package ru.nsu.fit.mihanizzm.litecrm.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Seller creating or updating request")
public record SellerRequestDto(
        @Schema(description = "Sellers name", example = "John")
        String name,

        @Schema(description = "Contact info of a seller", example = "john17@gmail.com")
        String contactInfo
) {}
