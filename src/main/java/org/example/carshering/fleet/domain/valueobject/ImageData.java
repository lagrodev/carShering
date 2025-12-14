package org.example.carshering.fleet.domain.valueobject;

/**
 * ImageData - Value Object для хранения данных изображения автомобиля

 */
public record ImageData(
        Long id,          // ID из БД (null для новых изображений)
        FileName fileName,
        ImageUrl url
) {
    /**
     * Compact constructor с валидацией
     */
    public ImageData {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName cannot be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("ImageUrl cannot be null");
        }
    }

    /**
     * Создать новое изображение (БЕЗ ID)
     * Используется при добавлении нового изображения
     *
     * @param fileName - имя файла
     * @param url      - URL изображения
     * @return новый ImageData без ID
     */
    public static ImageData create(FileName fileName, ImageUrl url) {
        return new ImageData(null, fileName, url);
    }

    /**
     * Восстановить изображение из БД (С ID)
     * Используется при загрузке из базы данных
     *
     * @param id       - ID из БД
     * @param fileName - имя файла
     * @param url      - URL изображения
     * @return ImageData с ID
     */
    public static ImageData restore(Long id, FileName fileName, ImageUrl url) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null when restoring from DB");
        }
        return new ImageData(id, fileName, url);
    }
}
