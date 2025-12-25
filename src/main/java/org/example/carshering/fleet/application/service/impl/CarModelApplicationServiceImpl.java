package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.api.dto.request.FilterCarModelRequest;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelRequest;
import org.example.carshering.fleet.api.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.example.carshering.fleet.application.mapper.CarModelDtoMapper;
import org.example.carshering.fleet.application.service.CarModelApplicationService;
import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.repository.BrandDomainRepository;
import org.example.carshering.fleet.domain.repository.CarClassDomainRepository;
import org.example.carshering.fleet.domain.repository.CarModelDomainRepository;
import org.example.carshering.fleet.domain.repository.ModelNameDomainRepository;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.id.CarClassId;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;
import org.example.carshering.fleet.domain.valueobject.id.ModelNameId;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarModelApplicationServiceImpl implements CarModelApplicationService {

    private final BrandDomainRepository brandRepository;
    private final ModelNameDomainRepository modelNameRepository;
    private final CarClassDomainRepository carClassRepository;
    private final CarModelDomainRepository carModelRepository;
    private final CarModelDtoMapper mapper;

    @Override
    public void deleteModel(ModelId modelId) {
        CarModelDomain carModelDomain = carModelRepository.findById(modelId).orElseThrow(
                () -> new NotFoundException("CarModel not found")
        );
        carModelDomain.markAsDeleted();
        carModelRepository.save(carModelDomain);
    }

    @Override
    public CarModelDto createModel(CreateCarModelRequest request) {
        BrandId brandId = brandRepository.findByName(new BrandName(request.brand())).orElseThrow(
                () -> new NotFoundException("Brand not found")
        ).getId();

        CarClassId carClassId = carClassRepository.findByName(
                new CarClassName(request.carClass())).orElseThrow(
                () -> new NotFoundException("CarClass not found")
        ).getId();

        ModelNameId modelNameId = modelNameRepository.findByName(new ModelName(request.model())).orElseThrow(
                () -> new NotFoundException("ModelName not found")
        ).getId();

        CarModelDomain modelDomain = CarModelDomain.create(
                new BodyType(request.bodyType()),
                carClassId,
                modelNameId,
                brandId
        );

        return mapper.toDto(carModelRepository.save(modelDomain));
    }

    @Override
    public Page<CarModelDto> getAllModelsIncludingDeleted(FilterCarModelRequest request, Pageable pageable) {
        BrandName brand = null;
        CarClassName carClass = null;

        if (request.brand() != null && !request.brand().isEmpty()) {
            brand = brandRepository.findByName(new BrandName(request.brand())).orElseThrow(
                    () -> new NotFoundException("Brand not found")
            ).getName();
        }
        if (request.carClass() != null && !request.carClass().isEmpty()) {
            carClass = carClassRepository.findByName(
                    new CarClassName(request.carClass())).orElseThrow(
                    () -> new NotFoundException("CarClass not found")
            ).getName();
        }

        BodyType bodyType = request.bodyType() != null && !request.bodyType().isEmpty() ? new BodyType( request.bodyType())  : null;

        Page<CarModelDomain> page = carModelRepository.findByFilter(
                request.deleted(),
                brand,
                bodyType,
                carClass,
                pageable
        );
        return page.map(mapper::toDto);
    }

    @Override
    public CarModelDto getModelById(ModelId modelId) {
        return mapper.toDto(carModelRepository.findById(modelId).orElseThrow(
                () -> new NotFoundException("CarModel not found")
        ));
    }

    @Override
    public CarModelDto updateModel(ModelId modelId, UpdateCarModelRequest request) {
        CarModelDomain modelDomain = carModelRepository.findById(modelId).orElseThrow(
                () -> new NotFoundException("CarModel not found")
        );

        // Получаем новые ID если они изменились
        if (request.brand() != null) {
            BrandId brandId = brandRepository.findByName(new BrandName(request.brand())).orElseThrow(
                    () -> new NotFoundException("Brand not found")
            ).getId();
            modelDomain.updateBrand(brandId);
        }

        if (request.carClass() != null) {
            CarClassId carClassId = carClassRepository.findByName(
                    new CarClassName(request.carClass())).orElseThrow(
                    () -> new NotFoundException("CarClass not found")
            ).getId();
            modelDomain.updateCarClass(carClassId);
        }

        if (request.model() != null) {
            ModelNameId modelNameId = modelNameRepository.findByName(new ModelName(request.model())).orElseThrow(
                    () -> new NotFoundException("ModelName not found")
            ).getId();
            modelDomain.updateModel(modelNameId);
        }

        if (request.bodyType() != null) {
            modelDomain.updateBodyType(new BodyType(request.bodyType()));
        }

        CarModelDomain updatedModel = carModelRepository.save(modelDomain);
        return mapper.toDto(updatedModel);
    }

    @Override
    public List<String> findAllBodyTypes() {
        return carModelRepository.findDistinctBodyTypes();
    }
}
