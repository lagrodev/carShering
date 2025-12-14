package org.example.carshering.fleet.api.mapper;

import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Маппер между Application DTO и API Response для названия модели
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ModelNameApiMapper {

    ModelNameResponse toResponse(ModelNameDto dto);

    List<ModelNameResponse> toResponseList(List<ModelNameDto> dtos);
}

