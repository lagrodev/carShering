package org.example.carshering.fleet.application.mapper;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.example.carshering.fleet.application.dto.response.CarModelDto;
import org.example.carshering.fleet.application.dto.response.ImageDto;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.repository.CarModelDomainRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования CarDomain → CarDto
 */
@Component
@RequiredArgsConstructor
public class CarDtoMapper {

    private final CarModelDomainRepository carModelRepository;
    private final CarModelDtoMapper carModelDtoMapper;
    private final ImageDtoMapper imageDtoMapper;

    /**
     * Domain → DTO
     */
    public CarDto toDto(CarDomain domain) {
        if (domain == null) {
            return null;
        }

        // Получаем CarModel
        CarModelDto carModelDto = carModelRepository.findById(domain.getModelId())
                .map(carModelDtoMapper::toDto)
                .orElse(null);

        // Мапим изображения
        List<ImageDto> images = imageDtoMapper.toDtoList(domain.getImages());

        return new CarDto(
                domain.getId() != null ? domain.getId().value() : null,
                domain.getGosNumber().getValue(),
                domain.getVin().getValue(),
                domain.getDailyRate().getAmount(),
                domain.getDailyRate().getCurrencyCode(),
                domain.getYearOfIssue().getValue(),
                domain.getState().name(),
                carModelDto,
                images
        );
    }

    /**
     * Список Domain → Список DTO
     */
    public List<CarDto> toDtoList(List<CarDomain> domains) {
        if (domains == null || domains.isEmpty()) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDto)
                .toList();
    }
}

