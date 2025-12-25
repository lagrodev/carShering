package org.example.carshering.fleet.api.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new car model")
public record CreateCarModelRequest(
        @Schema(description = "Brand of the car", example = "Toyota")
        @NotBlank String brand,
        @Schema(description = "Model of the car", example = "Camry")
        @NotBlank String model,

        @Schema(description = "Body type of the car", example = "Sedan")
        @NotBlank String bodyType,

        @Schema(description = "Class of the car", example = "Business")
        @NotBlank String carClass
) {
}
