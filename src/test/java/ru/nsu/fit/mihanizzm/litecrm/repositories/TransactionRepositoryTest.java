package ru.nsu.fit.mihanizzm.litecrm.repositories;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.nsu.fit.mihanizzm.litecrm.models.PaymentType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Testcontainers
class TransactionRepositoryTest {
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void startContainer() {
        postgres.start();
    }

    @AfterAll
    static void stopContainer() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldStartDatabase() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldFindTopSellerByPeriod() {
        Seller seller1 = new Seller();
        seller1.setName("Миша");
        seller1.setContactInfo("misha@example.com");

        Seller seller2 = new Seller();
        seller2.setName("Боб");
        seller2.setContactInfo("bob@example.com");

        seller1 = sellerRepository.save(seller1);
        seller2 = sellerRepository.save(seller2);

        LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 31, 23, 59);

        Transaction transaction1 = new Transaction();
        transaction1.setSeller(seller1);
        transaction1.setAmount(new BigDecimal("5000.00"));
        transaction1.setTransactionDate(start.plusDays(1));
        transaction1.setPaymentType(PaymentType.CARD);

        Transaction transaction2 = new Transaction();
        transaction2.setSeller(seller2);
        transaction2.setAmount(new BigDecimal("2000.00"));
        transaction2.setTransactionDate(start.plusDays(2));
        transaction2.setPaymentType(PaymentType.CARD);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        transaction1.setTransactionDate(start.plusDays(1));
        transaction2.setTransactionDate(start.plusDays(2));

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        Optional<Object[]> result = transactionRepository.findTopSellerByPeriod(start, end);

        assertThat(result).isPresent();
        Object[] topSellerData = result.get();
        assertThat(topSellerData).isNotEmpty();

        Object[] topSeller = (Object[]) topSellerData[0];
        Seller topSellerEntity = (Seller) topSeller[0];
        BigDecimal totalAmount = (BigDecimal) topSeller[1];

        assertThat(topSellerEntity).isEqualTo(seller1);
        assertThat(totalAmount).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldFindSellersWithTotalAmountLessThan() {
        Seller seller1 = new Seller();
        seller1.setName("Миша");
        seller1.setContactInfo("misha@example.com");

        Seller seller2 = new Seller();
        seller2.setName("Боб");
        seller2.setContactInfo("bob@example.com");

        seller1 = sellerRepository.save(seller1);
        seller2 = sellerRepository.save(seller2);

        LocalDateTime start = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 31, 23, 59);

        Transaction transaction1 = new Transaction();
        transaction1.setSeller(seller1);
        transaction1.setAmount(new BigDecimal("5000.00"));
        transaction1.setTransactionDate(start.plusDays(1));
        transaction1.setPaymentType(PaymentType.CARD);

        Transaction transaction2 = new Transaction();
        transaction2.setSeller(seller2);
        transaction2.setAmount(new BigDecimal("200.00"));
        transaction2.setTransactionDate(start.plusDays(2));
        transaction2.setPaymentType(PaymentType.CARD);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        transaction1.setTransactionDate(start.plusDays(1));
        transaction2.setTransactionDate(start.plusDays(2));

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        List<Seller> result = transactionRepository.findSellersWithTotalAmountLessThan(new BigDecimal("1000.00"), start, end);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(seller2);
    }

    @Test
    void shouldFindBestDayForSeller() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");
        seller = sellerRepository.save(seller);

        LocalDateTime day1 = LocalDateTime.of(2024, 3, 10, 10, 0);
        LocalDateTime day2 = LocalDateTime.of(2024, 3, 11, 10, 0);

        Transaction transaction1 = new Transaction();
        transaction1.setSeller(seller);
        transaction1.setAmount(new BigDecimal("5000.00"));
        transaction1.setTransactionDate(day1);
        transaction1.setPaymentType(PaymentType.CARD);

        Transaction transaction2 = new Transaction();
        transaction2.setSeller(seller);
        transaction2.setAmount(new BigDecimal("1000.00"));
        transaction2.setTransactionDate(day1);
        transaction2.setPaymentType(PaymentType.CARD);

        Transaction transaction3 = new Transaction();
        transaction3.setSeller(seller);
        transaction3.setAmount(new BigDecimal("3000.00"));
        transaction3.setTransactionDate(day2);
        transaction3.setPaymentType(PaymentType.CARD);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);

        transaction1.setTransactionDate(day1);
        transaction2.setTransactionDate(day1);
        transaction3.setTransactionDate(day2);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);


        Object result = transactionRepository.findBestDayForSellerNative(seller.getId());

        assertThat(result).isNotNull();
        Object[] resultArray = (Object[]) result;
        assertThat(resultArray).hasSize(2);
        assertThat(resultArray[0]).isEqualTo(java.sql.Date.valueOf(day1.toLocalDate())); // День с наибольшим числом транзакций
    }

    @Test
    void shouldSaveAndFindTransactionById() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");
        seller = sellerRepository.save(seller);

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentType(PaymentType.CARD);

        Transaction savedTransaction = transactionRepository.save(transaction);

        Optional<Transaction> foundTransaction = transactionRepository.findById(savedTransaction.getId());

        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getAmount()).isEqualByComparingTo("5000.00");
        assertThat(foundTransaction.get().getSeller()).isEqualTo(seller);
    }

    @Test
    void shouldFindAllTransactions() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");
        seller = sellerRepository.save(seller);

        Transaction transaction1 = new Transaction();
        transaction1.setSeller(seller);
        transaction1.setAmount(new BigDecimal("1000.00"));
        transaction1.setTransactionDate(LocalDateTime.now().minusDays(1));
        transaction1.setPaymentType(PaymentType.CARD);

        Transaction transaction2 = new Transaction();
        transaction2.setSeller(seller);
        transaction2.setAmount(new BigDecimal("2000.00"));
        transaction2.setTransactionDate(LocalDateTime.now());
        transaction2.setPaymentType(PaymentType.CARD);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        List<Transaction> transactions = transactionRepository.findAll();

        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getAmount).containsExactlyInAnyOrder(
                new BigDecimal("1000.00"), new BigDecimal("2000.00"));
    }

    @Test
    void shouldUpdateTransactionAmount() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");
        seller = sellerRepository.save(seller);

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentType(PaymentType.CARD);

        transaction = transactionRepository.save(transaction);

        transaction.setAmount(new BigDecimal("5000.00"));
        transaction = transactionRepository.save(transaction);

        Optional<Transaction> updatedTransaction = transactionRepository.findById(transaction.getId());

        assertThat(updatedTransaction).isPresent();
        assertThat(updatedTransaction.get().getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldDeleteTransactionById() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");
        seller = sellerRepository.save(seller);

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("3000.00"));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setPaymentType(PaymentType.CARD);

        transaction = transactionRepository.save(transaction);

        transactionRepository.deleteById(transaction.getId());

        Optional<Transaction> deletedTransaction = transactionRepository.findById(transaction.getId());
        assertThat(deletedTransaction).isEmpty();
    }
}