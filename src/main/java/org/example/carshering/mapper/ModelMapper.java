package org.example.carshering.mapper;
import org.example.carshering.dto.request.CreateCarModelRequest;
import org.example.carshering.dto.request.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ModelMapper {

    CarModel toEntity(CreateCarModelRequest request);

    CarModelResponse toDto(CarModel entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCarFromDto(UpdateCarModelRequest carDto, @MappingTarget CarModel model);

}
