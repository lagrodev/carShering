package org.example.carshering.fleet.application.dto.response;

/**
 * DTO для конфигурации модели автомобиля
 */
public record CarModelDto(
        Long id,
        String bodyType,
        String brand,
        String modelName,
        String carClass,
        boolean deleted
) {}

