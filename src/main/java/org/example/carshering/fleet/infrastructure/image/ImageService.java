package org.example.carshering.fleet.infrastructure.image;

import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.springframework.web.multipart.MultipartFile;

/**
 * Infrastructure Service для работы с файловым хранилищем (MinIO)
 * 
 * ВАЖНО: Работает ТОЛЬКО с файлами, НЕ с базой данных!
 * Сохранение в БД происходит через ImageRepositoryAdapter
 */
public interface ImageService {
    
    /**
     * Загрузить файл в MinIO
     * 
     * @param carId ID автомобиля (для организации файлов в папки)
     * @param file Файл изображения
     * @return ImageData (Value Object) с fileName и URL
     */
    ImageData uploadToStorage(Long carId, MultipartFile file);
    
    /**
     * Удалить файл из MinIO
     * 
     * @param fileName Имя файла для удаления
     */
    void deleteFromStorage(String fileName);
    
    /**
     * Получить публичный URL изображения
     * 
     * @param fileName Имя файла
     * @return Публичный URL
     */
    String getPublicUrl(String fileName);
}
