package ru.nsu.fit.mihanizzm.litecrm.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.fit.mihanizzm.litecrm.exception.NoTransactionsInThisPeriodException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerHasNoTransactionsException;
import ru.nsu.fit.mihanizzm.litecrm.models.PeriodType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.BestDayResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.SellerMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.TransactionRepository;
import ru.nsu.fit.mihanizzm.litecrm.services.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {
    private final TransactionRepository transactionRepository;
    private final SellerMapper sellerMapper;

    @Transactional(readOnly = true)
    @Override
    public SellerResponseDto getTopSeller(PeriodType type, LocalDateTime startDate) {
        log.info(
                "calculating top seller for period: {} and start date: {}",
                type,
                startDate
        );

        LocalDateTime start;
        LocalDateTime end;

        switch (type) {
            case DAY -> {
                start = startDate.toLocalDate().atStartOfDay();
                end = start.plusDays(1);
            }
            case MONTH -> {
                start = startDate.withDayOfMonth(1).toLocalDate().atStartOfDay();
                end = start.plusMonths(1);
            }
            case QUARTER -> {
                int currentMonth = startDate.getMonthValue();
                int startMonth = currentMonth - (currentMonth % 3) + 1;
                start = LocalDateTime.of(
                        startDate.getYear(),
                        startMonth,
                        1,
                        0,
                        0
                );
                end = start.plusMonths(3);
            }
            case YEAR -> {
                start = LocalDateTime.of(
                        startDate.getYear(),
                        1,
                        1,
                        0,
                        0
                );
                end = start.plusYears(1);
            }
            default -> throw new IllegalStateException(
                    String.format(
                            "Unexpected value: '%s'. PeriodType has values: 'DAY', 'MONTH', 'QUARTER', YEAR",
                            type
                    )
            );
        }

        Optional<Object[]> resultOpt = transactionRepository.findTopSellerByPeriod(start, end);
        if (resultOpt.isEmpty() || resultOpt.get().length == 0) {
            throw new NoTransactionsInThisPeriodException(start, end);
        }
        Object[] result = resultOpt.get();
        Object[] topSellerObj = (Object[]) result[0];

        Seller seller = (Seller) topSellerObj[0];
        log.info("successfully found top seller for period: {} and start date: {}",
                type,
                startDate
        );

        return sellerMapper.toResponse(seller);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SellerResponseDto> getSellersWithTotalAmountLessThan(
            BigDecimal maxSum,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        log.info(
                "finding sellers with max sum of {} in the period {} to {}",
                maxSum,
                startDate,
                endDate
        );

        List<Seller> sellers = transactionRepository.findSellersWithTotalAmountLessThan(
                maxSum,
                startDate,
                endDate
        );
        log.info(
                "found {} sellers with max sum of {} in the period {} to {}",
                sellers.size(),
                maxSum,
                startDate,
                endDate
        );

        return sellers.stream()
                .map(sellerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public BestDayResponseDto getBestDayForSeller(Integer sellerId) {
        log.info("calculating best day for seller: {}", sellerId);
        Object result = transactionRepository.findBestDayForSellerNative(sellerId);
        if (result == null) {
            throw new SellerHasNoTransactionsException(sellerId);
        }
        Object[] arr = (Object[]) result;
        if (arr.length == 0) {
            throw new SellerHasNoTransactionsException(sellerId);
        }
        LocalDate bestDay = ((java.sql.Date) arr[0]).toLocalDate();
        log.info("successfully calculated best day for seller: {}", sellerId);

        return new BestDayResponseDto(bestDay);
    }
}
