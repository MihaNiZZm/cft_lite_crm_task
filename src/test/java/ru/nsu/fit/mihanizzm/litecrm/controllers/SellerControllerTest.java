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
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.SellerService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SellerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerController sellerController;

    private SellerResponseDto sellerResponseDto;
    private SellerRequestDto sellerRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sellerController).build();

        sellerRequestDto = new SellerRequestDto("Миша", "misha@example.com");
        sellerResponseDto = new SellerResponseDto(
                1,
                "Миша",
                "misha@example.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0)
        );
    }

    @Test
    void shouldReturnAllSellers() throws Exception {
        given(sellerService.getAllSellers()).willReturn(List.of(sellerResponseDto));

        mockMvc.perform(get("/api/v1/sellers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Миша"))
                .andExpect(jsonPath("$[0].contactInfo").value("misha@example.com"));

        verify(sellerService).getAllSellers();
    }

    @Test
    void shouldReturnSellerById() throws Exception {
        given(sellerService.getSellerById(1)).willReturn(sellerResponseDto);

        mockMvc.perform(get("/api/v1/sellers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Миша"))
                .andExpect(jsonPath("$.contactInfo").value("misha@example.com"));

        verify(sellerService).getSellerById(1);
    }

    @Test
    void shouldReturnTransactionsBySellerId() throws Exception {
        given(sellerService.getSellerTransactions(1)).willReturn(List.of(new TransactionResponseDto(
                1,
                1,
                BigDecimal.valueOf(50.0),
                "CARD",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0)
        )));

        mockMvc.perform(get("/api/v1/sellers/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(sellerService).getSellerTransactions(1);
    }

    @Test
    void shouldCreateSeller() throws Exception {
        given(sellerService.createSeller(sellerRequestDto)).willReturn(sellerResponseDto);

        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Миша\", \"contactInfo\": \"misha@example.com\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Миша"))
                .andExpect(jsonPath("$.contactInfo").value("misha@example.com")
        );

        verify(sellerService).createSeller(sellerRequestDto);
    }

    @Test
    void shouldUpdateSeller() throws Exception {
        given(sellerService
                .updateSeller(
                        1,
                        new SellerRequestDto("Миша Обновленный", "misha_updated@example.com")))
                .willReturn(sellerResponseDto);

        mockMvc.perform(put("/api/v1/sellers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Миша Обновленный\", \"contactInfo\": \"misha_updated@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Миша"))
                .andExpect(jsonPath("$.contactInfo").value("misha@example.com"));

        verify(sellerService)
                .updateSeller(
                        1,
                        new SellerRequestDto("Миша Обновленный", "misha_updated@example.com"
                )
        );
    }

    @Test
    void shouldDeleteSeller() throws Exception {
        mockMvc.perform(delete("/api/v1/sellers/1"))
                .andExpect(status().isNoContent());

        verify(sellerService).deleteSeller(1);
    }
}