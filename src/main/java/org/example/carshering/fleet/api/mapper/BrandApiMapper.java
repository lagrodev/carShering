package org.example.carshering.fleet.api.mapper;

import org.example.carshering.fleet.api.dto.responce.BrandResponse;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Маппер между Application DTO и API Response для бренда
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BrandApiMapper {

    BrandResponse toResponse(BrandDto dto);

    List<BrandResponse> toResponseList(List<BrandDto> dtos);
}

