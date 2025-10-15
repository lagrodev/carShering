package org.example.carshering.mapper;

import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {



    Contract toEntity(CreateContractRequest request);

    @Mapping(source = "car.model.brand", target = "brand")
    @Mapping(source = "car.model.model", target = "model")
    @Mapping(source = "car.model.bodyType", target = "bodyType")
    @Mapping(source = "car.model.carClass", target = "carClass")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue")
    @Mapping(source = "client.firstName", target = "firstName")
    @Mapping(source = "dataStart", target = "startDate")
    @Mapping(source = "dataEnd", target = "endDate")
    ContractResponse toDto(Contract contract);

}
