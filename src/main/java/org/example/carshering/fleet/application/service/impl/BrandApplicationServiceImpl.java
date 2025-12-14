package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.request.CreateBrandRequest;
import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.example.carshering.fleet.application.mapper.BrandDtoMapper;
import org.example.carshering.fleet.application.service.BrandApplicationService;
import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.example.carshering.fleet.domain.repository.BrandDomainRepository;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandApplicationServiceImpl implements BrandApplicationService {
    private final BrandDtoMapper mapper;
    private final BrandDomainRepository brandRepository;

    @Override
    public BrandDto createBrand(CreateCarModelsBrand request) {
        if (brandRepository.existsByName(new BrandName(request.name()))) {
            throw new BusinessException("Brand already exists");
        }
        CarBrandDomain brand = CarBrandDomain.create(new BrandName(request.name()));

        CarBrandDomain saved = brandRepository.save(brand);

        return mapper.toDto(saved);
    }

    @Override
    public List<BrandDto> getAllBrands() {
        List<CarBrandDomain> brands = brandRepository.findAll();
        return brands.stream()
                .map(mapper::toDto)
                .toList();
    }
}
