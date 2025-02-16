package ru.nsu.fit.mihanizzm.litecrm.services.impl;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nsu.fit.mihanizzm.litecrm.exception.InvalidPaymentTypeException;
import ru.nsu.fit.mihanizzm.litecrm.exception.NullSellerIdException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.TransactionValidationException;
import ru.nsu.fit.mihanizzm.litecrm.models.PaymentType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.TransactionMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.SellerRepository;
import ru.nsu.fit.mihanizzm.litecrm.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        Validator validator = validatorFactory.getValidator();
        transactionService = new TransactionServiceImpl(
                transactionRepository,
                transactionMapper,
                sellerRepository,
                validator
        );
    }

    @Test
    void shouldGetAllTransactions() {
        Integer id = 1;
        Seller seller = new Seller(
                id,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Transaction transaction1 = new Transaction(
                1,
                null,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2025, Month.FEBRUARY, 14, 0, 0)
        );

        Transaction transaction2 = new Transaction(
                2,
                null,
                BigDecimal.valueOf(100.0),
                PaymentType.TRANSFER,
                LocalDateTime.of(2025, Month.FEBRUARY, 17, 0, 0)
        );

        seller.addTransaction(transaction1);
        seller.addTransaction(transaction2);

        when(transactionRepository.findAll()).thenReturn(List.of(transaction1, transaction2));

        List<TransactionResponseDto> result = transactionService.getAllTransactions();
        List<TransactionResponseDto> expected = Stream.of(transaction1, transaction2)
                .map(transactionMapper::toResponse)
                .toList();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(expected);

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void shouldGetNoTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.getAllTransactions();
        List<TransactionResponseDto> expected = List.of();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
        assertThat(result).isEqualTo(expected);

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void shouldGetTransactionById() {
        Integer id = 1;
        Seller seller = new Seller(
                1,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Transaction transaction = new Transaction(
                id,
                seller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2025, Month.FEBRUARY, 14, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        TransactionResponseDto result = transactionService.getTransactionById(id);
        TransactionResponseDto expected = transactionMapper.toResponse(transaction);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        verify(transactionRepository, times(1)).findById(id);
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionOnGetTransactionById() {
        Integer id = 1;

        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(id))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(transactionRepository, times(1)).findById(id);
    }

    @Test
    void shouldCreateTransaction() {
        Seller seller = new Seller(
                1,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        TransactionRequestDto request = new TransactionRequestDto(
                1,
                BigDecimal.valueOf(50.0),
                "CARD"
        );

        when(sellerRepository.findById(1)).thenReturn(Optional.of(seller));

        TransactionResponseDto result = transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();

        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getSeller()).isEqualTo(seller);
        assertThat(savedTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(50.0));
        assertThat(savedTransaction.getPaymentType()).isEqualTo(PaymentType.CARD);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(savedTransaction.getId());
        assertThat(result.sellerId()).isEqualTo(seller.getId());
        assertThat(result.amount()).isEqualTo(savedTransaction.getAmount());
        assertThat(result.paymentType()).isEqualTo(savedTransaction.getPaymentType().name());
        assertThat(result.transactionDate()).isEqualTo(savedTransaction.getTransactionDate());

        verify(sellerRepository, times(1)).findById(1);
        verify(transactionRepository, times(1)).save(savedTransaction);
    }

    @Test
    void shouldThrowNullSellerIdExceptionOnCreateTransaction() {
        TransactionRequestDto request = new TransactionRequestDto(
                null,
                BigDecimal.valueOf(50.0),
                "CARD"
        );

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(NullSellerIdException.class);

        verify(sellerRepository, times(0)).findById(any(Integer.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowSellerNotFoundExceptionOnCreateTransaction() {
        Integer id = 1;
        TransactionRequestDto request = new TransactionRequestDto(
                id,
                BigDecimal.valueOf(50.0),
                "CARD"
        );

        when(sellerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(sellerRepository, times(1)).findById(id);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowInvalidPaymentTypeExceptionOnCreateTransaction() {
        Integer sellerId = 1;
        Seller seller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        String invalidType = "invalid type";
        TransactionRequestDto request = new TransactionRequestDto(
                sellerId,
                BigDecimal.valueOf(50.0),
                invalidType
        );

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(InvalidPaymentTypeException.class)
                .hasFieldOrPropertyWithValue("passedPaymentType", invalidType);

        verify(sellerRepository, times(1)).findById(sellerId);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldNotValidateNegativeAmountOnCreateTransaction() {
        Integer sellerId = 1;
        Seller seller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        BigDecimal invalidAmount = BigDecimal.valueOf(-50.0);
        TransactionRequestDto request = new TransactionRequestDto(
                sellerId,
                invalidAmount,
                "CARD"
        );

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(1)).findById(sellerId);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldNotValidateNullAmountOnCreateTransaction() {
        Integer sellerId = 1;
        Seller seller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        BigDecimal invalidAmount = null;
        TransactionRequestDto request = new TransactionRequestDto(
                sellerId,
                invalidAmount,
                "CARD"
        );

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(1)).findById(sellerId);
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldUpdateTransactionWithAllFields() {
        Integer sellerId = 1;
        Seller oldSeller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Integer newSellerId = 2;
        Seller newSeller = new Seller(
                newSellerId,
                "John",
                "john@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        TransactionRequestDto request = new TransactionRequestDto(
                newSellerId,
                BigDecimal.valueOf(100.0),
                "TRANSFER"
        );

        Integer id = 1;
        Transaction oldTransaction = new Transaction(
                id,
                oldSeller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(oldTransaction));
        when(sellerRepository.findById(newSellerId)).thenReturn(Optional.of(newSeller));

        TransactionResponseDto result = transactionService.updateTransaction(id, request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction updatedTransaction = transactionCaptor.getValue();

        assertThat(updatedTransaction.getId()).isEqualTo(id);
        assertThat(updatedTransaction.getSeller()).isEqualTo(newSeller);
        assertThat(updatedTransaction.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(updatedTransaction.getAmount()).isEqualTo(request.amount());

        assertThat(result.id()).isEqualTo(updatedTransaction.getId());
        assertThat(result.transactionDate()).isEqualTo(updatedTransaction.getTransactionDate());
        assertThat(result.paymentType()).isEqualTo(updatedTransaction.getPaymentType().name());
        assertThat(result.sellerId()).isEqualTo(updatedTransaction.getSeller().getId());
        assertThat(result.amount()).isEqualTo(updatedTransaction.getAmount());

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(1)).findById(newSellerId);
        verify(transactionRepository, times(1)).save(updatedTransaction);
    }

    @Test
    void shouldUpdateTransactionWithoutNewSeller() {
        Integer sellerId = 1;
        Seller oldSeller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        TransactionRequestDto request = new TransactionRequestDto(
                null,
                BigDecimal.valueOf(100.0),
                "TRANSFER"
        );

        Integer id = 1;
        Transaction oldTransaction = new Transaction(
                id,
                oldSeller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(oldTransaction));

        TransactionResponseDto result = transactionService.updateTransaction(id, request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction updatedTransaction = transactionCaptor.getValue();

        assertThat(updatedTransaction.getId()).isEqualTo(id);
        assertThat(updatedTransaction.getSeller()).isEqualTo(oldSeller);
        assertThat(updatedTransaction.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(updatedTransaction.getAmount()).isEqualTo(request.amount());

        assertThat(result.id()).isEqualTo(updatedTransaction.getId());
        assertThat(result.transactionDate()).isEqualTo(updatedTransaction.getTransactionDate());
        assertThat(result.paymentType()).isEqualTo(updatedTransaction.getPaymentType().name());
        assertThat(result.sellerId()).isEqualTo(updatedTransaction.getSeller().getId());
        assertThat(result.amount()).isEqualTo(updatedTransaction.getAmount());

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(0)).findById(any(Integer.class));
        verify(transactionRepository, times(1)).save(updatedTransaction);
    }

    @Test
    void shouldUpdateTransactionWithoutNewAmount() {
        Integer sellerId = 1;
        Seller oldSeller = new Seller(
                sellerId,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Integer newSellerId = 2;
        Seller newSeller = new Seller(
                newSellerId,
                "John",
                "john@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        TransactionRequestDto request = new TransactionRequestDto(
                newSellerId,
                null,
                "TRANSFER"
        );

        Integer id = 1;
        Transaction oldTransaction = new Transaction(
                id,
                oldSeller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(oldTransaction));
        when(sellerRepository.findById(newSellerId)).thenReturn(Optional.of(newSeller));

        TransactionResponseDto result = transactionService.updateTransaction(id, request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction updatedTransaction = transactionCaptor.getValue();

        assertThat(updatedTransaction.getId()).isEqualTo(id);
        assertThat(updatedTransaction.getSeller()).isEqualTo(newSeller);
        assertThat(updatedTransaction.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(updatedTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(50.0));

        assertThat(result.id()).isEqualTo(updatedTransaction.getId());
        assertThat(result.transactionDate()).isEqualTo(updatedTransaction.getTransactionDate());
        assertThat(result.paymentType()).isEqualTo(updatedTransaction.getPaymentType().name());
        assertThat(result.sellerId()).isEqualTo(updatedTransaction.getSeller().getId());
        assertThat(result.amount()).isEqualTo(updatedTransaction.getAmount());

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(1)).findById(newSellerId);
        verify(transactionRepository, times(1)).save(updatedTransaction);
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionOnTransactionUpdate() {
        TransactionRequestDto request = new TransactionRequestDto(
                null,
                null,
                null
        );
        Integer id = 1;

        when(transactionRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(id, request))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(0)).findById(any(Integer.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowSellerNotFoundExceptionOnTransactionUpdate() {
        Seller oldSeller = new Seller(
                11,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Integer invalidSellerId = 5;
        TransactionRequestDto request = new TransactionRequestDto(
                invalidSellerId,
                null,
                null
        );

        Integer id = 1;
        Transaction transaction = new Transaction(
                id,
                oldSeller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2025, Month.FEBRUARY, 14, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(sellerRepository.findById(invalidSellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(id, request))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", invalidSellerId);

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(1)).findById(any(Integer.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldNotValidateNegativeAmountOnTransactionUpdate() {
        Seller oldSeller = new Seller(
                11,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        BigDecimal invalidAmount = BigDecimal.valueOf(-50.0);
        TransactionRequestDto request = new TransactionRequestDto(
                null,
                invalidAmount,
                null
        );

        Integer id = 1;
        Transaction transaction = new Transaction(
                id,
                oldSeller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2025, Month.FEBRUARY, 14, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.updateTransaction(id, request))
                .isInstanceOf(TransactionValidationException.class)
                .hasFieldOrProperty("violations");

        verify(transactionRepository, times(1)).findById(id);
        verify(sellerRepository, times(0)).findById(any(Integer.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
    }

    @Test
    void shouldDeleteTransaction() {
        Seller seller = new Seller(
                1,
                "Bob",
                "bob@gmail.com",
                LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0),
                new ArrayList<>()
        );

        Integer id = 5;
        Transaction transaction = new Transaction(
                id,
                seller,
                BigDecimal.valueOf(50.0),
                PaymentType.CARD,
                LocalDateTime.of(2025, Month.FEBRUARY, 14, 0, 0)
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(id);

        verify(transactionRepository, times(1)).findById(id);
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionOnDeleteTransaction() {
        Integer id = 1;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(id))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);
    }
}