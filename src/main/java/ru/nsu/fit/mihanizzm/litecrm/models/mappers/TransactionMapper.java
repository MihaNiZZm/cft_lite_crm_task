package ru.nsu.fit.mihanizzm.litecrm.models.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.nsu.fit.mihanizzm.litecrm.exception.InvalidPaymentTypeException;
import ru.nsu.fit.mihanizzm.litecrm.models.PaymentType;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.Transaction;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "paymentType", source = "paymentType", qualifiedByName = "stringToEnum")
    @Mapping(target = "seller", ignore = true)
    Transaction toEntity(TransactionRequestDto transactionRequestDto);

    @Mapping(target = "sellerId", source = "seller", qualifiedByName = "sellerToId")
    TransactionResponseDto toResponse(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "paymentType", source = "paymentType", qualifiedByName = "stringToEnum")
    @Mapping(target = "seller", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRequestToEntity(TransactionRequestDto transactionRequestDto, @MappingTarget Transaction transaction);

    default void updateRequestToEntity(
            TransactionRequestDto transactionRequestDto,
            @MappingTarget Transaction transaction,
            Seller seller
    ) {
        updateRequestToEntity(transactionRequestDto, transaction);
        transaction.getSeller().removeTransaction(transaction);
        seller.addTransaction(transaction);
    }

    default Transaction toEntity(TransactionRequestDto transactionRequestDto, Seller seller) {
        Transaction transaction = toEntity(transactionRequestDto);
        seller.addTransaction(transaction);
        return transaction;
    }

    @Named("stringToEnum")
    default PaymentType stringToEnum(String value) {
        try {
            return PaymentType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentTypeException(value);
        }
    }

    @Named("sellerToId")
    default Integer sellerToId(Seller seller) {
        if (seller == null) {
            return null;
        }
        return seller.getId();
    }
}
