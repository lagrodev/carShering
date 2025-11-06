package org.example.carshering.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update car model information")
public record UpdateCarModelRequest(

        @Schema(description = "Brand name", example = "Toyota")
        @NotBlank String brand,

        @Schema(description = "Model name", example = "Camry")
        @NotBlank String model,

        @Schema(description = "Body type", example = "Sedan")
        @NotBlank String bodyType,

        @Schema(description = "Car class", example = "Business")
        @NotBlank String carClass
) {
}
