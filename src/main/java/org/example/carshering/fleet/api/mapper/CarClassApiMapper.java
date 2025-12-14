package org.example.carshering.fleet.api.mapper;

import org.example.carshering.fleet.api.dto.responce.CarClassResponse;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Маппер между Application DTO и API Response для класса автомобиля
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarClassApiMapper {

    ModelNameResponse toResponse(CarClassDto dto);

    List<ModelNameResponse> toResponseList(List<CarClassDto> dtos);
}

