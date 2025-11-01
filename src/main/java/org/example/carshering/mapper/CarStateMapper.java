package org.example.carshering.mapper;

import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.CarState;
import org.example.carshering.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarStateMapper {
    CarStateResponse toDto(CarState client);

}
