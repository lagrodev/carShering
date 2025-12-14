package org.example.carshering.rental.application.mapper;

import org.example.carshering.rental.application.dto.response.ContractDto;
import org.example.carshering.rental.domain.model.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContractMapperForApplication {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "clientId", source = "clientId.value")
    @Mapping(target = "carId", source = "carId.value")
    @Mapping(target = "startDate", source = "rentalPeriod.startDate")
    @Mapping(target = "endDate", source = "rentalPeriod.endDate")
    @Mapping(target = "totalCost", source = "totalCost.amount")
    @Mapping(target = "state", source = "state")
    public abstract ContractDto toDto(Contract entity);
}
