package org.example.carshering.fleet.application.dto.response;

/**
 * DTO для изображения автомобиля
 * Используется для передачи изображений клиенту
 */
public record ImageDto(
        Long id,        // ID изображения (для операций удаления/обновления)
        String url      // Полный URL изображения (для отображения в <img>)
) {
}
