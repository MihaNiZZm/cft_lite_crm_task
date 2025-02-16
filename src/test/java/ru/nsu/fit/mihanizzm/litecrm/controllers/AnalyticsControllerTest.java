package ru.nsu.fit.mihanizzm.litecrm.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.nsu.fit.mihanizzm.litecrm.models.PeriodType;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.BestDayResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private SellerResponseDto sellerResponseDto;
    private BestDayResponseDto bestDayResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();

        sellerResponseDto = new SellerResponseDto(
                1,
                "Миша",
                "misha@example.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0)
        );
        bestDayResponseDto = new BestDayResponseDto(LocalDate.of(2024, Month.MAY, 1));
    }

    @Test
    void shouldReturnTopSellerByPeriod() throws Exception {
        LocalDateTime referenceDate = LocalDateTime.of(2024, 6, 7, 0, 0);
        given(analyticsService.getTopSeller(PeriodType.DAY, referenceDate)).willReturn(sellerResponseDto);

        mockMvc.perform(get("/api/v1/analytics/top-seller")
                        .param("period", "DAY")
                        .param("referenceDate", "2024-06-07T00:00:00.000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Миша"))
                .andExpect(jsonPath("$.contactInfo").value("misha@example.com"));

        verify(analyticsService).getTopSeller(PeriodType.DAY, referenceDate);
    }

    @Test
    void shouldReturnSellersWithTotalAmountLessThan() throws Exception {
        BigDecimal threshold = new BigDecimal("1000.00");
        LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 4, 1, 0, 0);
        given(analyticsService.getSellersWithTotalAmountLessThan(threshold, start, end))
                .willReturn(List.of(sellerResponseDto));

        mockMvc.perform(get("/api/v1/analytics/sellers-max-sum")
                        .param("threshold", "1000.00")
                        .param("start", "2024-03-01T00:00:00.000")
                        .param("end", "2024-04-01T00:00:00.000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Миша"))
                .andExpect(jsonPath("$[0].contactInfo").value("misha@example.com"));

        verify(analyticsService).getSellersWithTotalAmountLessThan(threshold, start, end);
    }

    @Test
    void shouldReturnBestDayForSeller() throws Exception {
        given(analyticsService.getBestDayForSeller(1)).willReturn(bestDayResponseDto);

        mockMvc.perform(get("/api/v1/analytics/best-day/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bestDay[0]").value(2024))
                .andExpect(jsonPath("$.bestDay[1]").value(5))
                .andExpect(jsonPath("$.bestDay[2]").value(1)); // Месяц, день и год

        verify(analyticsService).getBestDayForSeller(1);
    }
}