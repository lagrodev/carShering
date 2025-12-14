package org.example.carshering.fleet.application.service;

import org.example.carshering.fleet.application.dto.response.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Application Service для работы с изображениями автомобилей
 *
 * Координирует загрузку файлов в MinIO и сохранение метаданных в БД
 */
public interface CarImageApplicationService {

    /**
     * Загрузить изображения для автомобиля
     *
     * @param carId ID автомобиля
     * @param files Файлы изображений
     */
    void uploadImages(Long carId, List<MultipartFile> files);

    /**
     * Удалить изображение автомобиля
     *
     * @param carId ID автомобиля
     * @param fileName Имя файла для удаления
     */
    void deleteImage(Long carId, String fileName);

    /**
     * Получить все изображения автомобиля
     *
     * @param carId ID автомобиля
     * @return Список изображений
     */
    List<ImageDto> getCarImages(Long carId);
}

