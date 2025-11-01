package org.example.carshering.mapper;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.entity.*;
import org.example.carshering.repository.BrandRepository;
import org.example.carshering.repository.CarClassRepository;
import org.example.carshering.repository.ModelRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ModelMapper {
    @Autowired
    protected BrandRepository brandRepository;

    @Autowired
    protected ModelRepository modelRepository;

    @Autowired
    protected CarClassRepository carClassRepository;

    public abstract CarModel toEntity(CreateCarModelRequest request);

    @Mapping(source = "brand.name", target = "brand")
    @Mapping(source = "model.name", target = "model")
    @Mapping(source = "bodyType", target = "bodyType")
    @Mapping(source = "idModel", target = "modelId")
    @Mapping(source = "carClass.name", target = "carClass")
    public abstract CarModelResponse toDto(CarModel entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateCarFromDto(UpdateCarModelRequest carDto, @MappingTarget CarModel model);

    protected Brand mapBrand(String name) {
        return brandRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + name));
    }

    protected Model mapModel(String name) {
        return modelRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + name));
    }

    protected CarClass mapCarClass(String name) {
        return carClassRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("CarClass not found: " + name));
    }
}
