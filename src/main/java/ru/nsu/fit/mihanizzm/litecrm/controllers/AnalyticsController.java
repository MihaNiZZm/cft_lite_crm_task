package ru.nsu.fit.mihanizzm.litecrm.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.fit.mihanizzm.litecrm.models.PeriodType;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.BestDayResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Returns top seller in the given period",
            description = "Returns the top seller in the given period by searching it in the database. " +
                    "Uses ISO date-time format. This method defines the start and end dates using the date passed as " +
                    "a parameter. " +
                    "For example, parameters 'period' = 'DAY' and 'date' = '2025-06-07T00:00:00.000Z' " +
                    "will result in start date = " +
                    "'2025-01-01T00:00:00.000Z' and end date = '2026-01-01T00:00:00.000Z'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No transactions found"
            )
    })
    @GetMapping("/top-seller")
    public ResponseEntity<SellerResponseDto> getTopSeller(
            @RequestParam("period") PeriodType period,
            @RequestParam("referenceDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime referenceDate
    ) {
        return ResponseEntity.ok(analyticsService.getTopSeller(period, referenceDate));
    }

    @Operation(
            summary = "Returns sellers whose max sum is less than the parameter",
            description =
                    "Returns all sellers whose max sum of transactions is less than given parameter " +
                    "in the given period of time by searching it in the database. " +
                    "Uses ISO date-time format."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            )
    })
    @GetMapping("/sellers-max-sum")
    public ResponseEntity<List<SellerResponseDto>> getSellersWithTotalAmountLessThan(
            @RequestParam("threshold") BigDecimal threshold,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(analyticsService.getSellersWithTotalAmountLessThan(threshold, start, end));
    }

    @Operation(
            summary = "Returns the best day of the seller",
            description =
                    "Returns the best day of the seller based on the number of transactions by " +
                    "searching this information in the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seller has no transactions"
            )
    })
    @GetMapping("/best-day/{id}")
    public ResponseEntity<BestDayResponseDto> getBestDay(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(analyticsService.getBestDayForSeller(id));
    }
}
