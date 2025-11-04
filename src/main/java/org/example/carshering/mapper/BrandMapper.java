package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BrandMapper {

    public abstract Brand toEntity(CreateCarModelsBrand request);


    public abstract BrandModelResponse toDto(Brand entity);

}
