package ru.nsu.fit.mihanizzm.litecrm.services.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerValidationException;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.SellerMapper;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.TransactionMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.SellerRepository;
import ru.nsu.fit.mihanizzm.litecrm.services.SellerService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;
    private final TransactionMapper transactionMapper;
    private final Validator validator;

    @Transactional(readOnly = true)
    @Override
    public List<SellerResponseDto> getAllSellers() {
        log.info("finding all sellers");
        List<Seller> sellers = sellerRepository.findAll();
        log.info("successfully found {} sellers", sellers.size());

        return sellers.stream()
                .map(sellerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public SellerResponseDto getSellerById(Integer id) {
        log.info("getting a seller by id: {}", id);
        Seller seller = sellerRepository
                .findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));
        log.info("successfully found seller with id: {}", id);

        return sellerMapper.toResponse(seller);
    }

    @Transactional
    @Override
    public SellerResponseDto createSeller(SellerRequestDto sellerRequestDto) {
        log.info("creating a new seller");
        Seller seller = sellerMapper.toEntity(sellerRequestDto);
        validate(seller);
        sellerRepository.save(seller);
        log.info("successfully created a new seller");

        return sellerMapper.toResponse(seller);
    }

    @Transactional
    @Override
    public SellerResponseDto updateSeller(Integer id, SellerRequestDto sellerRequestDto) {
        log.info("finding a seller with id: {} to update", id);
        Seller seller = sellerRepository
                .findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));
        log.info("updating a seller with id: {}", id);
        sellerMapper.updateRequestToEntity(sellerRequestDto, seller);
        validate(seller);
        sellerRepository.save(seller);
        log.info("successfully updated a seller with id: {}", id);

        return sellerMapper.toResponse(seller);
    }

    @Transactional
    @Override
    public void deleteSeller(Integer id) {
        log.info("deleting a seller with id: {}", id);
        if (!sellerRepository.existsById(id)) {
            throw new SellerNotFoundException(id);
        }
        sellerRepository.deleteById(id);
        log.info("successfully deleted a seller with id: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getSellerTransactions(Integer id) {
        log.info("finding a seller by id: {} to get transactions", id);
        Seller seller = sellerRepository
                .findById(id)
                .orElseThrow(() -> new SellerNotFoundException(id));
        log.info("getting a seller transactions with id: {}", id);
        List<Transaction> transactions = seller.getTransactions();
        log.info("successfully found seller transactions by id: {}", id);

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    private void validate(Seller seller) {
        Set<ConstraintViolation<Seller>> errors = validator.validate(seller);
        if (!errors.isEmpty()) {
            throw new SellerValidationException(errors);
        }
    }
}
