package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.request.CreateBrandRequest;
import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.example.carshering.fleet.application.mapper.ModelNameDtoMapper;
import org.example.carshering.fleet.application.service.ModelNameApplicationService;
import org.example.carshering.fleet.domain.model.CarModelNameDomain;
import org.example.carshering.fleet.domain.repository.ModelNameDomainRepository;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelNameApplicationServiceImpl implements ModelNameApplicationService {
    private final ModelNameDtoMapper mapper;
    private final ModelNameDomainRepository repository;

    @Override
    public ModelNameDto createCarClass(CreateCarModelName request) {
        if (repository.existsByName(new ModelName(request.name()))) {
            throw new BusinessException("ModelName already exists");
        }
        CarModelNameDomain carClassDomain = CarModelNameDomain.create(new ModelName(request.name()));

        CarModelNameDomain saved = repository.save(carClassDomain);

        return mapper.toDto(saved);
    }

    @Override
    public List<ModelNameDto> getAllCarClass() {
        List<CarModelNameDomain> brands = repository.findAll();
        return brands.stream()
                .map(mapper::toDto)
                .toList();
    }
}
