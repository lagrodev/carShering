package org.example.carshering.fleet.api.mapper;

import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Маппер между Application DTO и API Response для автомобиля
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarApiMapper {

    @Mapping(target = "modelId", source = "dto.model.id")
    @Mapping(target = "brand", source = "dto.model.brand")
    @Mapping(target = "model", source = "dto.model.modelName")
    @Mapping(target = "bodyType", source = "dto.model.bodyType")
    @Mapping(target = "carClass", source = "dto.model.carClass")
    @Mapping(target = "yearOfIssue", source = "dto.year")
    @Mapping(target = "status", source = "dto.state")
    @Mapping(target = "rent", source = "dto.dailyRate")
    @Mapping(target = "favorite", source = "isFavorite")
    @Mapping(target = "imageUrl", expression = "java(dto.images() != null && !dto.images().isEmpty() ? dto.images().get(0).url() : null)")
    CarDetailResponse toDetailResponse(CarDto dto, boolean isFavorite);

    @Mapping(target = "brand", source = "dto.model.brand")
    @Mapping(target = "carClass", source = "dto.model.carClass")
    @Mapping(target = "model", source = "dto.model.modelName")
    @Mapping(target = "yearOfIssue", source = "dto.year")
    @Mapping(target = "rent", source = "dto.dailyRate")
    @Mapping(target = "status", source = "dto.state")
    @Mapping(target = "favorite", source = "isFavorite")
    CarListItemResponse toListItemResponse(CarDto dto, boolean isFavorite);
}

