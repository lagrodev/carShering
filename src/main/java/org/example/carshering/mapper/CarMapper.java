package org.example.carshering.mapper;

import org.example.carshering.domain.valueobject.GosNumber;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.domain.valueobject.Vin;
import org.example.carshering.domain.valueobject.Year;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.CarModel;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CarModelRepository;
import org.mapstruct.*;
import org.mapstruct.MappingConstants.ComponentModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Mapper(componentModel = ComponentModel.SPRING)
public abstract class CarMapper {
    //todo убрать @Autowired - он ненадежный. + убрать CarModelRepository в принципе из маппера
    @Autowired
    protected CarModelRepository carModelRepository;


    @Mapping(source = "car.model.brand.name", target = "brand")
    @Mapping(source = "car.model.model.name", target = "model")
    @Mapping(source = "car.model.bodyType", target = "bodyType")
    @Mapping(source = "car.model.idModel", target = "modelId")
    @Mapping(source = "car.model.carClass.name", target = "carClass")
    @Mapping(source = "car.state.status", target = "status")
    @Mapping(source = "favorite", target = "favorite")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue", qualifiedByName = "yearToInteger")
    @Mapping(source = "car.gosNumber", target = "gosNumber", qualifiedByName = "gosNumberToString")
    @Mapping(source = "car.vin", target = "vin", qualifiedByName = "vinToString")
    @Mapping(source = "car.dailyRate", target = "rent", qualifiedByName = "moneyToBigDecimal")
    public abstract CarDetailResponse toDetailDto(Car car, boolean favorite, String imageUrl);

    @Mapping(source = "car.model.brand.name", target = "brand")
    @Mapping(source = "car.model.model.name", target = "model")
    @Mapping(source = "car.model.carClass.name", target = "carClass")
    @Mapping(source = "car.state.status", target = "status")
    @Mapping(source = "favorite", target = "favorite")
    @Mapping(source = "car.yearOfIssue", target = "yearOfIssue", qualifiedByName = "yearToInteger")
    @Mapping(source = "car.dailyRate", target = "rent", qualifiedByName = "moneyToBigDecimal")
    public abstract CarListItemResponse toListItemDto(Car car, boolean favorite);


    @Mapping(target = "model", source = "modelId", qualifiedByName = "carModelIdToCarModel")
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "yearOfIssue", source = "yearOfIssue", qualifiedByName = "integerToYear")
    @Mapping(target = "gosNumber", source = "gosNumber", qualifiedByName = "stringToGosNumber")
    @Mapping(target = "vin", source = "vin", qualifiedByName = "stringToVin")
    @Mapping(target = "dailyRate", source = "rent", qualifiedByName = "bigDecimalToMoney")
    public abstract Car toEntity(CreateCarRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "vin", source = "request.vin", qualifiedByName = "stringToVin")
    @Mapping(target = "gosNumber", source = "request.gosNumber", qualifiedByName = "stringToGosNumber")
    @Mapping(target = "yearOfIssue", source = "request.yearOfIssue", qualifiedByName = "integerToYear")
    @Mapping(target = "dailyRate", source = "request.rent", qualifiedByName = "bigDecimalToMoney")
    public abstract void updateCar(@MappingTarget Car car, UpdateCarRequest request);

    @Named("stringToGosNumber")
    protected GosNumber stringToGosNumber(String gosNumber) {
        return gosNumber != null ? GosNumber.of(gosNumber) : null;
    }

    @Named("moneyToBigDecimal")
    protected BigDecimal moneyToBigDecimal(Money money) {
        return money != null ? money.getAmount() : null;
    }

    @Named("stringToVin")
    protected Vin stringToVin(String vin) {
        return vin != null ? Vin.of(vin) : null;
    }

    @Named("integerToYear")
    protected Year integerToYear(Integer year) {
        return year != null ? Year.of(year) : null;
    }

    @Named("yearToInteger")
    protected Integer yearToInteger(Year year) {
        return year != null ? year.getValue() : null;
    }

    @Named("gosNumberToString")
    protected String gosNumberToString(GosNumber gosNumber) {
        return gosNumber != null ? gosNumber.getValue() : null;
    }

    @Named("vinToString")
    protected String vinToString(Vin vin) {
        return vin != null ? vin.getValue() : null;
    }

    @Named("bigDecimalToMoney")
    protected Money bigDecimalToMoney(BigDecimal rent) {
        return rent != null ? Money.rubles(rent) : null;
    }

    @Named("carModelIdToCarModel")
    protected CarModel carModelIdToCarModel(Long modelId) {
        if (modelId == null) {
            return null;
        }

        return carModelRepository.findById(modelId)
                .orElseThrow(() -> new NotFoundException("CarModel not found with id: " + modelId));
    }
}