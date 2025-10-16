package org.example.carshering.mapper;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ModelMapper {

    CarModel toEntity(CreateCarModelRequest request);

    CarModelResponse toDto(CarModel entity);
}
