package org.example.carshering.mapper;

import org.example.carshering.domain.valueobject.GosNumber;
import org.example.carshering.domain.valueobject.Money;
import org.example.carshering.domain.valueobject.RentalPeriod;
import org.example.carshering.domain.valueobject.Year;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.domain.entity.Contract;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContractMapper {


    @Mapping(target = "period", source = "request")
    public abstract Contract toEntity(CreateContractRequest request);

    @Mapping(source = "car.model.brand.name", target = "brand")
    @Mapping(source = "car.model.model.name", target = "model")
    @Mapping(source = "car.model.bodyType", target = "bodyType")
    @Mapping(source = "car.model.carClass.name", target = "carClass")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue", qualifiedByName = "yearToInteger")
    @Mapping(source = "car.gosNumber", target = "gosNumber", qualifiedByName = "gosNumberToString")
    @Mapping(source = "car.vin", target = "vin", qualifiedByName = "vinToString")
    @Mapping(source = "client.lastName", target = "lastName")
    @Mapping(source = "period.startDate", target = "startDate")
    @Mapping(source = "period.endDate", target = "endDate")
    @Mapping(source = "state.name", target = "state")
    @Mapping(source = "totalCost", target = "totalCost", qualifiedByName = "moneyToBigDecimal")
    public abstract ContractResponse toDto(Contract contract);

    @Named("yearToInteger")
    protected Integer yearToInteger(Year year) {
        return year != null ? year.getValue() : null;
    }

    @Named("gosNumberToString")
    protected String gosNumberToString(Object gosNumber) {
        return gosNumber != null ? gosNumber.toString() : null;
    }

    @Named("totalCostToDouble")
    protected BigDecimal totalCostToDouble(Money totalCost) {
        return totalCost != null ? totalCost.getAmount() : null;
    }


    @Named("vinToString")
    protected String vinToString(Object vin) {
        return vin != null ? vin.toString() : null;
    }

    @Named("moneyToBigDecimal")
    public BigDecimal moneyToBigDecimal(Money money) {
        return money != null ? money.getAmount() : null;
    }

    protected RentalPeriod mapToRentalPeriod(CreateContractRequest request) {
        if (request == null) return null;
        return RentalPeriod.of(request.dataStart(), request.dataEnd());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateContractFromRequest(
            UpdateContractRequest request,
            @MappingTarget Contract contract
    );}
