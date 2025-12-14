package org.example.carshering.fleet.infrastructure.persistence.mapper;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.model.CarModelDomain;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.infrastructure.persistence.entity.Car;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Маппер для преобразования Car Entity -> CarDomain Aggregate
 * НЕ использует MapStruct, т.к. нужна кастомная логика для изображений
 */
@Component
public class CarMapperForDomain {

    /**
     * Преобразовать Car Entity в CarDomain без изображений
     * Используется, когда изображения не нужны
     *
     * @param entity - JPA entity
     * @return CarDomain aggregate
     */
    public CarDomain toDomain(Car entity) {
        return toDomain(entity, null);
    }

    /**
     * Преобразовать Car Entity в CarDomain с изображениями
     * Используется, когда изображения загружены отдельным запросом
     *
     * @param entity - JPA entity
     * @param images - список изображений (Value Objects)
     * @return CarDomain aggregate
     */
    public CarDomain toDomain(Car entity, List<ImageData> images) {
        if (entity == null) {
            return null;
        }

        return CarDomain.restore(
                entity.getId() != null ? new CarId(entity.getId()) : null,
                entity.getGosNumber(),
                entity.getVin(),
                entity.getDailyRate(),
                entity.getYearOfIssue(),
                entity.getModel(),
                entity.getState(),
                images // передаем изображения (может быть null или пустой список)
        );
    }

    public Car toEntity(CarDomain carModelDomain) {
        if (carModelDomain == null) {
            return null;
        }

        Car entity = new Car();

        if (carModelDomain.getId() != null) {
            entity.setId(carModelDomain.getId().value());
        }
        entity.setGosNumber(carModelDomain.getGosNumber());
        entity.setVin(carModelDomain.getVin());
        entity.setDailyRate(carModelDomain.getDailyRate());
        entity.setYearOfIssue(carModelDomain.getYearOfIssue());
        entity.setModel(carModelDomain.getModelId());
        entity.setState(carModelDomain.getState());

        return entity;
    }
}
