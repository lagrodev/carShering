package org.example.carshering.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new car model name")
public record CreateCarModelName(
        @Schema(description = "Name of the car model", example = "Civic")
        @NotBlank String name
        )
{}
