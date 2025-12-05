package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.domain.entity.CarClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class CarClassMapper {

    public abstract CarClass toEntity(CreateCarModelName request);

    @Mapping(source = "name", target = "name")
    public abstract ModelNameResponse toDto(CarClass entity);

}
