package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.Brand;
import org.example.carshering.entity.Model;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ModelNameMapper {

    public abstract Model toEntity(CreateCarModelName request);


    public abstract ModelNameResponse toDto(Model entity);

}
