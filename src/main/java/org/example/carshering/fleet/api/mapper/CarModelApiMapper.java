package org.example.carshering.fleet.api.mapper;

import org.example.carshering.fleet.api.dto.responce.CarModelResponse;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Маппер между Application DTO и API Response для модели автомобиля
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarModelApiMapper {

    CarModelResponse toResponse(CarModelDto dto);

    List<CarModelResponse> toResponseList(List<CarModelDto> dtos);

    default Page<CarModelResponse> toResponsePage(Page<CarModelDto> dtoPage) {
        return dtoPage.map(this::toResponse);
    }
}

