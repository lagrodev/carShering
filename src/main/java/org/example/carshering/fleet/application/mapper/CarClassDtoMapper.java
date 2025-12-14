package org.example.carshering.fleet.application.mapper;

import org.example.carshering.fleet.application.dto.response.CarClassDto;
import org.example.carshering.fleet.domain.model.CarClassDomain;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования CarClassDomain → CarClassDto
 */
@Component
public class CarClassDtoMapper {

    /**
     * Domain → DTO
     */
    public CarClassDto toDto(CarClassDomain domain) {
        if (domain == null) {
            return null;
        }

        return new CarClassDto(
                domain.getId() != null ? domain.getId().value() : null,
                domain.getName().value()
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<CarClassDto> toDtoList(List<CarClassDomain> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}

