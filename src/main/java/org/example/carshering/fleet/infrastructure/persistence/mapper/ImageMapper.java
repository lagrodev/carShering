package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.infrastructure.persistence.entity.Image;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования Image Entity <-> ImageData Value Object
 */
@Component
public class ImageMapper {

    /**
     * Entity -> Value Object
     */
    public ImageData toValueObject(Image entity) {
        if (entity == null) {
            return null;
        }

        return ImageData.create(
                entity.getFileName(),
                entity.getUrl()
        );
    }

    /**
     * Value Object -> Entity
     * Требует CarId для связи с автомобилем
     */
    public Image toEntity(CarId carId, ImageData valueObject) {
        if (valueObject == null) {
            return null;
        }

        return Image.builder()
                .car(carId)
                .fileName(valueObject.fileName())
                .url(valueObject.url())
                .build();
    }
}

