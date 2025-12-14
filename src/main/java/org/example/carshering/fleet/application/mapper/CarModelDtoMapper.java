package org.example.carshering.fleet.application.mapper;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.repository.BrandDomainRepository;
import org.example.carshering.fleet.domain.repository.CarClassDomainRepository;
import org.example.carshering.fleet.domain.repository.ModelNameDomainRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования CarModelDomain → CarModelDto
 *
 * ВАЖНО: Для получения названий нужно обращаться к репозиториям
 * CarModelDomain содержит только ID (BrandId, ModelNameId, CarClassId)
 */
@Component
@RequiredArgsConstructor
public class CarModelDtoMapper {

    private final BrandDomainRepository brandRepository;
    private final ModelNameDomainRepository modelNameRepository;
    private final CarClassDomainRepository carClassRepository;

    /**
     * Domain → DTO
     */
    public CarModelDto toDto(CarModelDomain domain) {
        if (domain == null) {
            return null;
        }

        // Получаем названия из репозиториев (из кэша!)
        String brandName = brandRepository.findById(domain.getBrand())
                .map(b -> b.getName().value())
                .orElse("Unknown");

        String modelName = modelNameRepository.findById(domain.getModel())
                .map(m -> m.getName().value())
                .orElse("Unknown");

        String carClassName = carClassRepository.findById(domain.getCarClass())
                .map(c -> c.getName().value())
                .orElse("Unknown");

        return new CarModelDto(
                domain.getModelId() != null ? domain.getModelId().value() : null,
                domain.getBodyType().value(),
                brandName,
                modelName,
                carClassName,
                domain.isDeleted()
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<CarModelDto> toDtoList(List<CarModelDomain> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}

