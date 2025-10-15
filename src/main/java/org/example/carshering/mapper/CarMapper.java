package org.example.carshering.mapper;

import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;


@Mapper(componentModel = ComponentModel.SPRING)
public interface CarMapper {

    @Mapping(source = "model.brand", target = "brand")
    @Mapping(source = "model.model", target = "model")
    @Mapping(source = "model.bodyType", target = "bodyType")
    @Mapping(source = "model.carClass", target = "carClass")
    @Mapping(source = "state.status", target = "status")
    CarDetailResponse toDetailDto(Car car);


    @Mapping(source = "model.brand", target = "brand")
    @Mapping(source = "model.carClass", target = "carClass")
    @Mapping(source = "model.model", target = "model")
    CarListItemResponse toListItemDto(Car car);
}
