package org.example.carshering.fleet.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.application.dto.response.ImageDto;
import org.example.carshering.fleet.application.mapper.ImageDtoMapper;
import org.example.carshering.fleet.application.service.CarImageApplicationService;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.repository.CarDomainRepository;
import org.example.carshering.fleet.domain.valueobject.FileName;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.infrastructure.image.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Application Service для управления изображениями автомобилей
 *
 * Координирует:
 * 1. Загрузку файлов в MinIO (через ImageService)
 * 2. Добавление метаданных в Domain модель
 * 3. Сохранение в БД (через CarDomainRepository → ImageRepositoryAdapter)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarImageApplicationServiceImpl implements CarImageApplicationService {

    private final CarDomainRepository carRepository;
    private final ImageService imageService;
    private final ImageDtoMapper imageDtoMapper;

    @Override
    @Transactional
    public void uploadImages(Long carId, List<MultipartFile> files) {
        log.info("Uploading {} images for car: {}", files.size(), carId);

        // 1. Получаем Domain объект
        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // 2. Загружаем файлы в MinIO через ImageService
        List<ImageData> uploadedImages = files.stream()
                .map(file -> imageService.uploadToStorage(carId, file))
                .toList();

        // 3. Добавляем изображения через Domain методы
        uploadedImages.forEach(img ->
            car.addImage(img.fileName(), img.url())
        );

        // 4. Сохраняем (ImageRepositoryAdapter синхронизирует с БД)
        carRepository.save(car);

        log.info("Successfully uploaded {} images for car: {}", files.size(), carId);
    }

    @Override
    @Transactional
    public void deleteImage(Long carId, String fileName) {
        log.info("Deleting image {} for car: {}", fileName, carId);

        // 1. Получаем Domain объект
        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        // 2. Удаляем из Domain модели
        car.removeImageByFileName(new FileName(fileName));

        // 3. Удаляем из MinIO
        imageService.deleteFromStorage(fileName);

        // 4. Сохраняем (ImageRepositoryAdapter синхронизирует с БД)
        carRepository.save(car);

        log.info("Successfully deleted image {} for car: {}", fileName, carId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageDto> getCarImages(Long carId) {
        log.debug("Getting images for car: {}", carId);

        CarId id = new CarId(carId);
        CarDomain car = carRepository.findById(id);

        if (car == null) {
            throw new NotFoundException("Car with id " + carId + " not found");
        }

        return car.getImages().stream()
                .map(imageDtoMapper::toDto)
                .toList();
    }
}

