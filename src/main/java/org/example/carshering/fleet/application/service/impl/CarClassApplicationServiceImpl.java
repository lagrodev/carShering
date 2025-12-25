package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.example.carshering.fleet.application.mapper.CarClassDtoMapper;
import org.example.carshering.fleet.application.service.CarClassApplicationService;
import org.example.carshering.fleet.domain.model.CarClassDomain;
import org.example.carshering.fleet.domain.repository.CarClassDomainRepository;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarClassApplicationServiceImpl implements CarClassApplicationService {
    private final CarClassDtoMapper mapper;
    private final CarClassDomainRepository carClassRepository;

    @Override
    public CarClassDto createCarClass(CreateCarModelName request) {
        if (carClassRepository.existsByName(new CarClassName(request.name()))) {
            throw new BusinessException("CarClass already exists");
        }
        CarClassDomain carClassDomain = CarClassDomain.create(new CarClassName(request.name()));

        CarClassDomain saved = carClassRepository.save(carClassDomain);

        return mapper.toDto(saved);
    }

    @Override
    public List<CarClassDto> getAllCarClass() {
        List<CarClassDomain> brands = carClassRepository.findAll();
        return brands.stream()
                .map(mapper::toDto)
                .toList();
    }
}
