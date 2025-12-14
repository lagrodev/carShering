package org.example.carshering.fleet.application.mapper;

import org.example.carshering.fleet.application.dto.response.ImageDto;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования ImageData (Domain) → ImageDto (Application)
 */
@Component
public class ImageDtoMapper {

    /**
     * ImageData (Domain Value Object) → ImageDto
     *
     * ImageData теперь содержит ID из БД
     */
    public ImageDto toDto(ImageData imageData) {
        if (imageData == null) {
            return null;
        }

        return new ImageDto(
                imageData.id(),           // ID из Domain (может быть null для новых)
                imageData.url().value()   // Извлекаем String из ImageUrl
        );
    }

    /**
     * Список ImageData → Список ImageDto
     */
    public List<ImageDto> toDtoList(List<ImageData> imageDataList) {
        if (imageDataList == null || imageDataList.isEmpty()) {
            return List.of();
        }

        return imageDataList.stream()
                .map(this::toDto)
                .toList();
    }
}

