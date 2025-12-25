package org.example.carshering.fleet.api.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new car model brand")
public record CreateCarModelsBrand(
        @Schema(description = "Name of the car brand", example = "Toyota")
        @NotBlank String name
        )
{}
