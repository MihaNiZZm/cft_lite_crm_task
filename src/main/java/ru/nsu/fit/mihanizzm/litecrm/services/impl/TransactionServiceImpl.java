package ru.nsu.fit.mihanizzm.litecrm.services.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.fit.mihanizzm.litecrm.exception.NullSellerIdException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionValidationException;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.TransactionMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.SellerRepository;
import ru.nsu.fit.mihanizzm.litecrm.repositories.TransactionRepository;
import ru.nsu.fit.mihanizzm.litecrm.services.TransactionService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SellerRepository sellerRepository;
    private final Validator validator;

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getAllTransactions() {
        log.info("finding all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        log.info("successfully found {} transactions", transactions.size());

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionResponseDto getTransactionById(Integer id) {
        log.info("finding a transaction by id {}", id);
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        log.info("successfully found a transaction with id {}", id);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    @Override
    public TransactionResponseDto createTransaction(TransactionRequestDto transactionRequestDto) {
        log.info("trying to create a new transaction");
        if (transactionRequestDto.sellerId() == null) {
            throw new NullSellerIdException();
        }
        log.info("finding a seller_id {}", transactionRequestDto.sellerId());
        Seller seller = sellerRepository
                .findById(transactionRequestDto.sellerId())
                .orElseThrow(() -> new SellerNotFoundException(transactionRequestDto.sellerId()));
        log.info("successfully found a seller with id: {}", transactionRequestDto.sellerId());
        Transaction transaction = transactionMapper.toEntity(transactionRequestDto, seller);
        validate(transaction);
        log.info("successfully created a new transaction with id: {}", transaction.getId());
        transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    @Override
    public TransactionResponseDto updateTransaction(Integer id, TransactionRequestDto transactionRequestDto) {
        log.info("finding a transaction with id: {} to update", id);
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        log.info("successfully found a transaction with id: {} to update", id);
        log.info("updating a transaction with id {}", id);

        if (transactionRequestDto.sellerId() == null) {
            transactionMapper.updateRequestToEntity(transactionRequestDto, transaction);
        }
        else {
            log.info("finding a seller of transaction with id: {} to update", id);
            Seller seller = sellerRepository
                    .findById(transactionRequestDto.sellerId())
                    .orElseThrow(() -> new SellerNotFoundException(transactionRequestDto.sellerId()));
            log.info("successfully found a seller of transaction with id: {} to update", id);
            if (Objects.equals(seller.getId(), transaction.getSeller().getId())) {
                transactionMapper.updateRequestToEntity(transactionRequestDto, transaction);
            }
            else {
                transactionMapper.updateRequestToEntity(transactionRequestDto, transaction, seller);
            }
        }

        validate(transaction);
        transactionRepository.save(transaction);
        log.info("successfully updated a transaction with id: {}", id);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    @Override
    public void deleteTransaction(Integer id) {
        log.info("finding a transaction with id: {} to delete", id);
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        if (transaction.getSeller() != null) {
            log.info("unlinking a seller from a transaction with id: {} to remove", id);
            transaction.getSeller().removeTransaction(transaction);
            log.info("successfully unlinked a seller from a transaction with id: {} to remove", id);
        }
        transactionRepository.delete(transaction);
        log.info("successfully deleted a transaction with id: {}", id);
    }

    private void validate(Transaction transaction) {
        Set<ConstraintViolation<Transaction>> errors = validator.validate(transaction);
        if (!errors.isEmpty()) {
            throw new TransactionValidationException(errors);
        }
    }
}
