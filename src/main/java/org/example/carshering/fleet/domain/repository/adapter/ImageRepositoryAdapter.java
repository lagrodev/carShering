package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.fleet.domain.repository.ImageDomainRepository;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.infrastructure.persistence.entity.Image;
import org.example.carshering.fleet.infrastructure.persistence.mapper.ImageMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.ImageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Адаптер для работы с изображениями автомобилей
 * этот репозиторий используется внутри CarRepositoryAdapter
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ImageRepositoryAdapter implements ImageDomainRepository {

    private final ImageRepository imageRepository;
    private final ImageMapper mapper;

    /**
     * Найти все изображения для автомобиля
     */
    @Override
    @Transactional(readOnly = true)
    public List<ImageData> findByCarId(CarId carId) {
        log.debug("Loading images for car: {}", carId.value());

        return imageRepository.findByCarId(carId.value())
                .stream()
                .map(mapper::toValueObject)
                .collect(Collectors.toList());
    }

    /**
     * Заменить все изображения автомобиля
     * DELETE ALL + INSERT ALL подход
     */
    @Override
    @Transactional
    public void replaceImages(CarId carId, List<ImageData> images) {
        log.debug("Replacing all images for car: {}", carId.value());

        // 1. Удаляем все старые изображения
        imageRepository.deleteByCarId(carId.value());

        // 2. Добавляем новые изображения
        if (images != null && !images.isEmpty()) {
            addImages(carId, images);
        }
    }

    /**
     * Добавить изображения к автомобилю
     * Не удаляет существующие
     */
    @Override
    @Transactional
    public void addImages(CarId carId, List<ImageData> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        log.debug("Adding {} images for car: {}", images.size(), carId.value());

        List<Image> imageEntities = images.stream()
                .map(img -> mapper.toEntity(carId, img))
                .collect(Collectors.toList());

        imageRepository.saveAll(imageEntities);
    }

    /**
     * Удалить все изображения автомобиля
     */
    @Override
    @Transactional
    public void deleteByCarId(CarId carId) {
        log.debug("Deleting all images for car: {}", carId.value());
        imageRepository.deleteByCarId(carId.value());
    }
}

