package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.Brand;
import org.example.carshering.entity.CarClass;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.Model;
import org.example.carshering.repository.BrandRepository;
import org.example.carshering.repository.CarClassRepository;
import org.example.carshering.repository.ModelRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BrandMapper {

    public abstract Brand toEntity(CreateCarModelsBrand request);


    public abstract BrandModelResponse toDto(Brand entity);

}
