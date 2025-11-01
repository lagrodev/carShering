package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.CarClass;
import org.example.carshering.entity.Model;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class CarClassMapper {

    public abstract CarClass toEntity(CreateCarModelName request);


    public abstract ModelNameResponse toDto(CarClass entity);

}
