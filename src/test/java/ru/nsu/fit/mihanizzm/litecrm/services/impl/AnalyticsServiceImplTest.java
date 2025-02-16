package ru.nsu.fit.mihanizzm.litecrm.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nsu.fit.mihanizzm.litecrm.exception.NoTransactionsInThisPeriodException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerHasNoTransactionsException;
import ru.nsu.fit.mihanizzm.litecrm.models.PeriodType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.BestDayResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.SellerMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    private final SellerMapper sellerMapper = Mappers.getMapper(SellerMapper.class);

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private Seller seller;
    private SellerResponseDto sellerResponseDto;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(
                transactionRepository,
                sellerMapper
        );

        seller = new Seller(
                1,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );
        sellerResponseDto = sellerMapper.toResponse(seller);
    }

    @Test
    void shouldReturnTopSeller() {
        LocalDateTime startDate = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime expectedStart = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime expectedEnd = expectedStart.plusMonths(1);

        Object[] topSellerObj = new Object[]{seller, 5000.00};
        Optional<Object[]> resultOpt = Optional.of(new Object[]{topSellerObj});

        when(transactionRepository.findTopSellerByPeriod(expectedStart, expectedEnd)).thenReturn(resultOpt);

        SellerResponseDto result = analyticsService.getTopSeller(PeriodType.MONTH, startDate);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(sellerResponseDto);

        verify(transactionRepository, times(1))
                .findTopSellerByPeriod(expectedStart, expectedEnd);
    }

    @Test
    void shouldThrowExceptionIfNoTransactions() {
        LocalDateTime startDate = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime expectedStart = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime expectedEnd = expectedStart.plusMonths(1);

        when(transactionRepository.findTopSellerByPeriod(expectedStart, expectedEnd)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getTopSeller(PeriodType.MONTH, startDate))
                .isInstanceOf(NoTransactionsInThisPeriodException.class)
                .hasFieldOrPropertyWithValue("start", expectedStart)
                .hasFieldOrPropertyWithValue("end", expectedEnd);

        verify(transactionRepository, times(1))
                .findTopSellerByPeriod(expectedStart, expectedEnd);
    }

    @Test
    void shouldReturnSellersWithTotalAmountLessThan() {
        BigDecimal maxSum = new BigDecimal("1000.00");
        LocalDateTime startDate = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 3, 31, 23, 59);

        List<Seller> sellers = List.of(seller);

        when(transactionRepository.findSellersWithTotalAmountLessThan(maxSum, startDate, endDate))
                .thenReturn(sellers);

        List<SellerResponseDto> result = analyticsService.getSellersWithTotalAmountLessThan(maxSum, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(sellerResponseDto);

        verify(transactionRepository, times(1))
                .findSellersWithTotalAmountLessThan(maxSum, startDate, endDate);
    }

    @Test
    void shouldReturnEmptyListIfNoSellersFound() {
        BigDecimal maxSum = new BigDecimal("1000.00");
        LocalDateTime startDate = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 3, 31, 23, 59);

        when(transactionRepository.findSellersWithTotalAmountLessThan(maxSum, startDate, endDate))
                .thenReturn(Collections.emptyList());

        List<SellerResponseDto> result = analyticsService.getSellersWithTotalAmountLessThan(maxSum, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);

        verify(transactionRepository, times(1))
                .findSellersWithTotalAmountLessThan(maxSum, startDate, endDate);
    }

    @Test
    void shouldReturnBestDayForSeller() {
        Integer sellerId = 1;
        LocalDate bestDay = LocalDate.of(2024, 3, 15);
        Object[] mockResult = new Object[]{Date.valueOf(bestDay)}; // Имитация ответа БД

        when(transactionRepository.findBestDayForSellerNative(sellerId)).thenReturn(mockResult);

        BestDayResponseDto result = analyticsService.getBestDayForSeller(sellerId);

        assertThat(result).isNotNull();
        assertThat(result.bestDay()).isEqualTo(bestDay);

        verify(transactionRepository, times(1)).findBestDayForSellerNative(sellerId);
    }

    @Test
    void shouldThrowExceptionIfNoTransactionsForSeller() {
        Integer sellerId = 1;
        when(transactionRepository.findBestDayForSellerNative(sellerId)).thenReturn(null);

        assertThatThrownBy(() -> analyticsService.getBestDayForSeller(sellerId))
                .isInstanceOf(SellerHasNoTransactionsException.class)
                .hasFieldOrPropertyWithValue("sellerId", sellerId);

        verify(transactionRepository, times(1)).findBestDayForSellerNative(sellerId);
    }

    @Test
    void shouldThrowExceptionIfEmptyResultArray() {
        Integer sellerId = 1;
        when(transactionRepository.findBestDayForSellerNative(sellerId)).thenReturn(new Object[]{});

        assertThatThrownBy(() -> analyticsService.getBestDayForSeller(sellerId))
                .isInstanceOf(SellerHasNoTransactionsException.class)
                .hasFieldOrPropertyWithValue("sellerId", sellerId);

        verify(transactionRepository, times(1)).findBestDayForSellerNative(sellerId);
    }
}