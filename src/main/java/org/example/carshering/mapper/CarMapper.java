package org.example.carshering.mapper;

import org.example.carshering.dto.request.CreateCarRequest;
import org.example.carshering.dto.request.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.repository.CarModelRepository;
import org.mapstruct.*;
import org.mapstruct.MappingConstants.ComponentModel;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = ComponentModel.SPRING)
public abstract class CarMapper {

    @Autowired
    protected CarModelRepository carModelRepository;

    @Mapping(source = "model.brand", target = "brand")
    @Mapping(source = "model.model", target = "model")
    @Mapping(source = "model.bodyType", target = "bodyType")
    @Mapping(source = "model.carClass", target = "carClass")
    @Mapping(source = "state.status", target = "status")
    public abstract CarDetailResponse toDetailDto(Car car);

    @Mapping(source = "model.brand", target = "brand")
    @Mapping(source = "model.carClass", target = "carClass")
    @Mapping(source = "model.model", target = "model")
    public abstract CarListItemResponse toListItemDto(Car car);

    @Mapping(target = "model", source = "model")
    @Mapping(target = "state", ignore = true)
    public abstract Car toEntity(CreateCarRequest createCarRequest, CarModel model);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateCar(@MappingTarget Car car, UpdateCarRequest request);

    protected CarModel carModelIdToCarModel(Long carModelId) {
        if (carModelId == null) {
            return null;
        }
        return carModelRepository.findById(carModelId)
                .orElseThrow(() -> new IllegalArgumentException("CarModel not found: " + carModelId));
    }

}
