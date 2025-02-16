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
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Testcontainers
class SellerRepositoryTest {
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private TestEntityManager entityManager;

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
    void shouldSaveAndFindSeller() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");

        seller = sellerRepository.save(seller);

        assertThat(seller.getId()).isNotNull();
        assertThat(sellerRepository.findById(seller.getId())).isPresent();
    }

    @Test
    void shouldSaveAndFindSellerById() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");

        Seller savedSeller = sellerRepository.save(seller);

        Optional<Seller> found = sellerRepository.findById(savedSeller.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Миша");
        assertThat(found.get().getContactInfo()).isEqualTo("misha@example.com");
    }

    @Test
    void shouldFindAllSellers() {
        Seller seller1 = new Seller();
        seller1.setName("Миша");
        seller1.setContactInfo("misha@example.com");

        Seller seller2 = new Seller();
        seller2.setName("Боб");
        seller2.setContactInfo("bob@example.com");

        entityManager.persist(seller1);
        entityManager.persist(seller2);
        entityManager.flush();

        List<Seller> sellers = sellerRepository.findAll();

        assertThat(sellers).hasSize(2);
        assertThat(sellers).extracting(Seller::getName).containsExactlyInAnyOrder("Миша", "Боб");
    }

    @Test
    void shouldDeleteSellerById() {
        Seller seller = new Seller();
        seller.setName("Миша");
        seller.setContactInfo("misha@example.com");

        entityManager.persist(seller);
        entityManager.flush();

        sellerRepository.deleteById(seller.getId());

        Optional<Seller> found = sellerRepository.findById(seller.getId());
        assertThat(found).isEmpty();
    }
}