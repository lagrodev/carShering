package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.Contract;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContractMapper {



    public abstract Contract toEntity(CreateContractRequest request);

    @Mapping(source = "car.model.brand.name", target = "brand")
    @Mapping(source = "car.model.model.name", target = "model")
    @Mapping(source = "car.model.bodyType", target = "bodyType")
    @Mapping(source = "car.model.carClass.name", target = "carClass")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue")
    @Mapping(source = "car.gosNumber", target = "gosNumber")
    @Mapping(source = "car.vin", target = "vin")
    @Mapping(source = "client.lastName", target = "lastName")
    @Mapping(source = "dataStart", target = "startDate")
    @Mapping(source = "dataEnd", target = "endDate")
    @Mapping(source = "state.name", target = "state")
    public abstract ContractResponse toDto(Contract contract);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateContractFromRequest(
            UpdateContractRequest request,
            @MappingTarget Contract contract
    );}
