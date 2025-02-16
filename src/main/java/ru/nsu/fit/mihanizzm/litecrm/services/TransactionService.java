package ru.nsu.fit.mihanizzm.litecrm.services;

import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;

import java.util.List;

public interface TransactionService {
    List<TransactionResponseDto> getAllTransactions();
    TransactionResponseDto getTransactionById(Integer id);
    TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto);
    TransactionResponseDto updateTransaction(Integer id, TransactionRequestDto transactionRequestDto);
    void deleteTransaction(Integer id);
}
