package org.example.carshering.mapper;

import org.example.carshering.dto.request.CreateContractRequest;
import org.example.carshering.dto.request.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Contract;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContractMapper {



    public abstract Contract toEntity(CreateContractRequest request);

    @Mapping(source = "car.model.brand", target = "brand")
    @Mapping(source = "car.model.model", target = "model")
    @Mapping(source = "car.model.bodyType", target = "bodyType")
    @Mapping(source = "car.model.carClass", target = "carClass")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue")
    @Mapping(source = "car.gosNumber", target = "gosNumber")
    @Mapping(source = "car.vin", target = "vin")
    @Mapping(source = "client.firstName", target = "firstName")
    @Mapping(source = "dataStart", target = "startDate")
    @Mapping(source = "dataEnd", target = "endDate")
    public abstract ContractResponse toDto(Contract contract);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateContractFromRequest(
            UpdateContractRequest request,
            @MappingTarget Contract contract
    );}
