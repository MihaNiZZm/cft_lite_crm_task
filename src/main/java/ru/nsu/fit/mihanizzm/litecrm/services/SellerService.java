package ru.nsu.fit.mihanizzm.litecrm.services;

import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;

import java.util.List;

public interface SellerService {
    List<SellerResponseDto> getAllSellers();
    SellerResponseDto getSellerById(Integer id);
    SellerResponseDto createSeller(SellerRequestDto sellerRequestDto);
    SellerResponseDto updateSeller(Integer id, SellerRequestDto sellerRequestDto);
    void deleteSeller(Integer id);
    List<TransactionResponseDto> getSellerTransactions(Integer id);
}
