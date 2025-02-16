package ru.nsu.fit.mihanizzm.litecrm.services;

import ru.nsu.fit.mihanizzm.litecrm.models.PeriodType;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.BestDayResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    SellerResponseDto getTopSeller(PeriodType type, LocalDateTime startDate);
    List<SellerResponseDto> getSellersWithTotalAmountLessThan(
            BigDecimal maxSum,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    BestDayResponseDto getBestDayForSeller(Integer sellerId);
}
