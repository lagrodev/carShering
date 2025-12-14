package org.example.carshering.fleet.application.mapper;

import org.example.carshering.fleet.application.dto.response.BrandDto;
import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования CarBrandDomain → BrandDto
 */
@Component
public class BrandDtoMapper {

    /**
     * Domain → DTO
     */
    public BrandDto toDto(CarBrandDomain domain) {
        if (domain == null) {
            return null;
        }

        return new BrandDto(
                domain.getId() != null ? domain.getId().value() : null,
                domain.getName().value()
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<BrandDto> toDtoList(List<CarBrandDomain> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}

