package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CarModelRepository;
import org.mapstruct.*;
import org.mapstruct.MappingConstants.ComponentModel;
import org.springframework.beans.factory.annotation.Autowired;

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
    public abstract CarDetailResponse toDetailDto(Car car, boolean favorite);

    @Mapping(source = "car.model.brand.name", target = "brand")
    @Mapping(source = "car.model.model.name", target = "model")
    @Mapping(source = "car.model.carClass.name", target = "carClass")
    @Mapping(source = "car.state.status", target = "status")
    @Mapping(source = "favorite", target = "favorite")
    public abstract CarListItemResponse toListItemDto(Car car, boolean favorite);


    @Mapping(target = "model", source = "modelId")
    @Mapping(target = "state", ignore = true)
    public abstract Car toEntity(CreateCarRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "model", ignore = true)
    public abstract void updateCar(@MappingTarget Car car, UpdateCarRequest request);


    protected CarModel carModelIdToCarModel(Long modelId) {
        if (modelId == null) {
            return null;
        }

        return carModelRepository.findById(modelId)
                .orElseThrow(() -> new NotFoundException("CarModel not found with id: " + modelId));
    }
}