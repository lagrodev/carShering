package org.example.carshering.fleet.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request для создания названия модели
 */
public record CreateModelNameRequest(
        @NotBlank(message = "Model name cannot be blank")
        String name
) {}

