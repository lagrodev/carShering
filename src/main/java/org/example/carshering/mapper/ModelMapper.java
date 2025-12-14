//package org.example.carshering.mapper;
//import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
//import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
//import org.example.carshering.fleet.infrastructure.persistence.entity.CarModel;
//import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
//import org.example.carshering.dto.request.create.CreateCarModelRequest;
//import org.example.carshering.dto.request.update.UpdateCarModelRequest;
//import org.example.carshering.fleet.api.dto.responce.CarModelResponse;
//import org.example.carshering.fleet.infrastructure.persistence.repository.BrandRepository;
//import org.example.carshering.fleet.infrastructure.persistence.repository.CarClassRepository;
//import org.example.carshering.fleet.infrastructure.persistence.repository.ModelNameRepository;
//import org.mapstruct.*;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
//public abstract class ModelMapper {
//    @Autowired
//    protected BrandRepository brandRepository;
//
//    @Autowired
//    protected ModelNameRepository modelRepository;
//
//    @Autowired
//    protected CarClassRepository carClassRepository;
//
//    public abstract CarModel toEntity(CreateCarModelRequest request);
//
//    @Mapping(source = "brand.name", target = "brand")
//    @Mapping(source = "model.name", target = "model")
//    @Mapping(source = "bodyType", target = "bodyType")
//    @Mapping(source = "idModel", target = "modelId")
//    @Mapping(source = "carClass.name", target = "carClass")
//    @Mapping(source = "deleted", target = "isDeleted")
//    public abstract CarModelResponse toDto(CarModel entity);
//
//
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    public abstract void updateCarFromDto(UpdateCarModelRequest carDto, @MappingTarget CarModel model);
//
//    protected Brand mapBrand(String name) {
//        return brandRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + name));
//    }
//
//    protected Model mapModel(String name) {
//        return modelRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + name));
//    }
//
//    protected CarClass mapCarClass(String name) {
//        return carClassRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new IllegalArgumentException("CarClass not found: " + name));
//    }
//}
