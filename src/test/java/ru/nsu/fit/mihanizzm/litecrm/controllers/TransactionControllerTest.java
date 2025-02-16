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
import ru.nsu.fit.mihanizzm.litecrm.models.PaymentType;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequestDto transactionRequestDto;
    private TransactionRequestDto transactionUpdateRequestDto;
    private TransactionResponseDto transactionResponseDto;
    private TransactionResponseDto transactionUpdateResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        transactionRequestDto = new TransactionRequestDto(
                1,
                BigDecimal.valueOf(50.0),
                "CARD"
        );
        transactionUpdateRequestDto = new TransactionRequestDto(
                1,
                BigDecimal.valueOf(1000.0),
                "CARD"
        );
        transactionResponseDto = new TransactionResponseDto(
                1,
                1,
                BigDecimal.valueOf(50.0),
                "CARD",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0)
        );
        transactionUpdateResponseDto = new TransactionResponseDto(
                1,
                1,
                BigDecimal.valueOf(1000.0),
                "CARD",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0)
        );
    }

    @Test
    void shouldReturnAllTransactions() throws Exception {
        given(transactionService.getAllTransactions()).willReturn(List.of(transactionResponseDto));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sellerId").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.0));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void shouldReturnTransactionById() throws Exception {
        given(transactionService.getTransactionById(1)).willReturn(transactionResponseDto);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.amount").value(50.0));

        verify(transactionService).getTransactionById(1);
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        given(transactionService.createTransaction(transactionRequestDto)).willReturn(transactionResponseDto);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerId\": 1, \"amount\": 50.0, \"paymentType\": \"CARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.amount").value(50.0));

        verify(transactionService).createTransaction(transactionRequestDto);
    }

    @Test
    void shouldUpdateTransaction() throws Exception {
        given(transactionService.updateTransaction(1, transactionUpdateRequestDto))
                .willReturn(transactionUpdateResponseDto);

        mockMvc.perform(put("/api/v1/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerId\": 1, \"amount\": 1000.0, \"paymentType\": \"CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.amount").value(1000.0));

        verify(transactionService).updateTransaction(1, transactionUpdateRequestDto);
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        mockMvc.perform(delete("/api/v1/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(1);
    }
}