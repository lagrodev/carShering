package org.example.carshering.fleet.application.mapper;

import org.example.carshering.fleet.application.dto.response.ModelNameDto;
import org.example.carshering.fleet.domain.model.CarModelNameDomain;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования CarModelNameDomain → ModelNameDto
 */
@Component
public class ModelNameDtoMapper {

    /**
     * Domain → DTO
     */
    public ModelNameDto toDto(CarModelNameDomain domain) {
        if (domain == null) {
            return null;
        }

        return new ModelNameDto(
                domain.getId() != null ? domain.getId().value() : null,
                domain.getName().value()
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<ModelNameDto> toDtoList(List<CarModelNameDomain> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}

