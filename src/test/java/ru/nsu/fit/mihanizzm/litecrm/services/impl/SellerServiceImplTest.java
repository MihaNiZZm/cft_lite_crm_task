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
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerNotFoundException;
import ru.nsu.fit.mihanizzm.litecrm.exception.SellerValidationException;
import ru.nsu.fit.mihanizzm.litecrm.models.PaymentType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.SellerMapper;
import ru.nsu.fit.mihanizzm.litecrm.models.mappers.TransactionMapper;
import ru.nsu.fit.mihanizzm.litecrm.repositories.SellerRepository;

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
class SellerServiceImplTest {
    @Mock
    private SellerRepository sellerRepository;

    private final SellerMapper sellerMapper = Mappers.getMapper(SellerMapper.class);
    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    @InjectMocks
    private SellerServiceImpl sellerService;

    @BeforeEach
    void setUp() {
        Validator validator = validatorFactory.getValidator();
        sellerService = new SellerServiceImpl(
                sellerRepository,
                sellerMapper,
                transactionMapper,
                validator
        );
    }

    @Test
    void shouldGetAllSellers() {
        Seller seller1 = new Seller();
        seller1.setId(1);
        seller1.setName("Bob");
        seller1.setContactInfo("bob@gmail.com");
        seller1.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        Seller seller2 = new Seller();
        seller2.setId(2);
        seller2.setName("John");
        seller2.setContactInfo("john@gmail.com");
        seller2.setRegistrationDate(LocalDateTime.of(2024, Month.JUNE, 1, 0, 0));

        when(sellerRepository.findAll()).thenReturn(List.of(seller1, seller2));

        List<SellerResponseDto> result = sellerService.getAllSellers();
        List<SellerResponseDto> expected = Stream.of(seller1, seller2)
                .map(sellerMapper::toResponse)
                .toList();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(expected);

        verify(sellerRepository, times(1)).findAll();
    }

    @Test
    void shouldGetNoSellers() {
        when(sellerRepository.findAll()).thenReturn(List.of());

        List<SellerResponseDto> result = sellerService.getAllSellers();
        List<SellerResponseDto> expected = List.of();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
        assertThat(result).isEqualTo(expected);

        verify(sellerRepository, times(1)).findAll();
    }

    @Test
    void shouldGetSellerById() {
        Seller seller = new Seller();
        seller.setId(1);
        seller.setName("Bob");
        seller.setContactInfo("bob@gmail.com");
        seller.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        when(sellerRepository.findById(1)).thenReturn(Optional.of(seller));

        SellerResponseDto result = sellerService.getSellerById(1);
        SellerResponseDto expected = sellerMapper.toResponse(seller);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);

        verify(sellerRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowExceptionIfSellerNotFoundOnGet() {
        Integer id = 1;

        when(sellerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerById(id))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(sellerRepository, times(1)).findById(1);
    }

    @Test
    void shouldCreateSeller() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "Bob",
                "bob@gmail.com"
        );

        SellerResponseDto result = sellerService.createSeller(sellerRequestDto);

        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());

        Seller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller).isNotNull();
        assertThat(sellerRequestDto.name()).isEqualTo(savedSeller.getName());
        assertThat(sellerRequestDto.contactInfo()).isEqualTo(savedSeller.getContactInfo());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(savedSeller.getId());
        assertThat(result.name()).isEqualTo(savedSeller.getName());
        assertThat(result.contactInfo()).isEqualTo(savedSeller.getContactInfo());
        assertThat(result.registrationDate()).isEqualTo(savedSeller.getRegistrationDate());

        verify(sellerRepository, times(1)).save(savedSeller);
    }

    @Test
    void shouldNoValidateNullName() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                null,
                "bob@gmail.com"
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldNoValidateBlankName() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "",
                "bob@gmail.com"
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldNoValidateShortName() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "a",
                "bob@gmail.com"
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldNoValidateLongName() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "a".repeat(256),
                "bob@gmail.com"
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldNoValidateLongContactInfo() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "Bob",
                "a".repeat(256)
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldNoValidateBlankContactInfo() {
        SellerRequestDto sellerRequestDto = new SellerRequestDto(
                "Bob",
                ""
        );

        Seller seller = sellerMapper.toEntity(sellerRequestDto);

        assertThatThrownBy(() -> sellerService.createSeller(sellerRequestDto))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(0)).save(seller); // Should not save invalid entity
    }

    @Test
    void shouldUpdateSellerWithAllFields() {
        SellerRequestDto request = new SellerRequestDto(
                "John",
                "john@gmail.com"
        );
        Integer id = 1;

        Seller seller = new Seller();
        seller.setId(id);
        seller.setName("Bob");
        seller.setContactInfo("bob@gmail.com");
        seller.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));

        SellerResponseDto result = sellerService.updateSeller(id, request);

        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());

        Seller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller.getId()).isEqualTo(id);
        assertThat(savedSeller.getName()).isEqualTo(request.name());
        assertThat(savedSeller.getContactInfo()).isEqualTo(request.contactInfo());

        assertThat(result.id()).isEqualTo(savedSeller.getId());
        assertThat(result.name()).isEqualTo(savedSeller.getName());
        assertThat(result.contactInfo()).isEqualTo(savedSeller.getContactInfo());
        assertThat(result.registrationDate()).isEqualTo(savedSeller.getRegistrationDate());

        verify(sellerRepository, times(1)).findById(id);
        verify(sellerRepository, times(1)).save(savedSeller);
    }

    @Test
    void shouldUpdateSellerWithSomeFields() {
        SellerRequestDto request = new SellerRequestDto(
                "John",
                null
        );
        Integer id = 1;
        String initialName = "Bob";
        String initialContactInfo = "bob@gmail.com";

        Seller seller = new Seller();
        seller.setId(id);
        seller.setName(initialName);
        seller.setContactInfo(initialContactInfo);
        seller.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));

        SellerResponseDto result = sellerService.updateSeller(id, request);

        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());

        Seller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller.getId()).isEqualTo(id);
        assertThat(savedSeller.getName()).isEqualTo(request.name());
        assertThat(savedSeller.getContactInfo()).isEqualTo(initialContactInfo);

        assertThat(result.id()).isEqualTo(savedSeller.getId());
        assertThat(result.name()).isEqualTo(savedSeller.getName());
        assertThat(result.contactInfo()).isEqualTo(savedSeller.getContactInfo());
        assertThat(result.registrationDate()).isEqualTo(savedSeller.getRegistrationDate());

        verify(sellerRepository, times(1)).findById(id);
        verify(sellerRepository, times(1)).save(savedSeller);
    }

    @Test
    void shouldNotValidateUpdatedSeller() {
        SellerRequestDto request = new SellerRequestDto(
                "J",
                "john@gmail.com"

        );
        Integer id = 1;
        String initialName = "Bob";
        String initialContactInfo = "bob@gmail.com";

        Seller seller = new Seller();
        seller.setId(id);
        seller.setName(initialName);
        seller.setContactInfo(initialContactInfo);
        seller.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> sellerService.updateSeller(id, request))
                .isInstanceOf(SellerValidationException.class)
                .hasFieldOrProperty("violations");

        verify(sellerRepository, times(1)).findById(id); // Should find the seller
        verify(sellerRepository, times(0)).save(seller); // Should not save entity
    }

    @Test
    void shouldThrowSellerNotFoundExceptionWhenSellerNotFoundOnUpdate() {
        SellerRequestDto request = new SellerRequestDto(
                "John",
                "john@gmail.com"

        );
        Integer id = 1;
        String initialName = "Bob";
        String initialContactInfo = "bob@gmail.com";

        Seller seller = new Seller();
        seller.setId(id);
        seller.setName(initialName);
        seller.setContactInfo(initialContactInfo);
        seller.setRegistrationDate(LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0));

        when(sellerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.updateSeller(id, request))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(sellerRepository, times(1)).findById(id); // Should try to find the seller
        verify(sellerRepository, times(0)).save(any(Seller.class)); // Should save nothing
    }

    @Test
    void shouldDeleteSeller() {
        Integer id = 1;

        when(sellerRepository.existsById(id)).thenReturn(true);

        sellerService.deleteSeller(id);

        verify(sellerRepository, times(1)).existsById(id);
        verify(sellerRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowSellerNotFoundExceptionWhenSellerNotFoundOnDelete() {
        Integer id = 1;

        when(sellerRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> sellerService.deleteSeller(id))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(sellerRepository, times(1)).existsById(id); // Should try to find the seller
        verify(sellerRepository, times(0)).deleteById(id); // Should delete nothing
    }

    @Test
    void shouldGetSellerTransactions() {
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

        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));

        List<TransactionResponseDto> result = sellerService.getSellerTransactions(1);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(transactionMapper.toResponse(transaction1));
        assertThat(result.get(1)).isEqualTo(transactionMapper.toResponse(transaction2));

        verify(sellerRepository, times(1)).findById(1);
    }

    @Test
    void shouldThrowSellerNotFoundExceptionWhenSellerNotFoundOnGetTransactions() {
        Integer id = 1;

        when(sellerRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sellerService.getSellerTransactions(id))
                .isInstanceOf(SellerNotFoundException.class)
                .hasFieldOrPropertyWithValue("id", id);

        verify(sellerRepository, times(1)).findById(1);
    }
}