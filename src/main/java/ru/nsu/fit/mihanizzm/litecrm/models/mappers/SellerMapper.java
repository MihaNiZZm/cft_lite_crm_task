package ru.nsu.fit.mihanizzm.litecrm.models.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.nsu.fit.mihanizzm.litecrm.models.Seller;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;

@Mapper(componentModel = "spring")
public interface SellerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    Seller toEntity(SellerRequestDto sellerRequestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRequestToEntity(SellerRequestDto sellerRequestDto, @MappingTarget Seller seller);

    SellerResponseDto toResponse(Seller seller);
}
