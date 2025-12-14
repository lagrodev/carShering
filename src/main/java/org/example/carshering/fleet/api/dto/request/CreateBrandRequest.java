package org.example.carshering.fleet.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request для создания бренда
 */
public record CreateBrandRequest(
        @NotBlank(message = "Brand name cannot be blank")
        String name
) {}

