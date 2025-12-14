package org.example.carshering.fleet.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request для создания конфигурации модели
 */
public record CreateCarModelRequest(
        @NotBlank(message = "Body type cannot be blank")
        String bodyType,

        @NotNull(message = "Brand ID cannot be null")
        Long brandId,

        @NotNull(message = "Model name ID cannot be null")
        Long modelNameId,

        @NotNull(message = "Car class ID cannot be null")
        Long carClassId
) {}

