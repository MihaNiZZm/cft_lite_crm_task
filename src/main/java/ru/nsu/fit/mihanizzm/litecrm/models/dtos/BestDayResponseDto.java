package ru.nsu.fit.mihanizzm.litecrm.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Best seller's day")
public record BestDayResponseDto(
        @Schema(description = "The day when seller got the most number of transactions", example = "2003-05-22")
        LocalDate bestDay
) {}
