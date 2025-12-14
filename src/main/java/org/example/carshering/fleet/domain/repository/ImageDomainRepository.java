package org.example.carshering.fleet.domain.repository;

import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.common.domain.valueobject.CarId;

import java.util.List;

/**
 * Domain Repository для изображений автомобилей
 * Управляется через CarDomain Aggregate
 */
public interface ImageDomainRepository {

    /**
     * Найти все изображения для автомобиля
     */
    List<ImageData> findByCarId(CarId carId);

    /**
     * Заменить все изображения автомобиля
     * DELETE ALL + INSERT ALL
     */
    void replaceImages(CarId carId, List<ImageData> images);

    /**
     * Добавить изображения к автомобилю
     * Не удаляет существующие
     */
    void addImages(CarId carId, List<ImageData> images);

    /**
     * Удалить все изображения автомобиля
     */
    void deleteByCarId(CarId carId);
}

