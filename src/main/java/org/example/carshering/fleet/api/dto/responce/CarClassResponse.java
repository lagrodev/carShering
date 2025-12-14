package org.example.carshering.fleet.api.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car class response")
public record CarClassResponse(
        @Schema(description = "Car class ID", example = "1")
        Long id,
        @Schema(description = "Car class name", example = "Business")
        String name
) {}

