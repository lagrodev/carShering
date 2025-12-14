package org.example.carshering.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update car model information")
public record UpdateCarModelRequest(

        @Schema(description = "Brand name", example = "Toyota")
        String brand,

        @Schema(description = "Model name", example = "Camry")
        String model,

        @Schema(description = "Body type", example = "Sedan")
        String bodyType,

        @Schema(description = "Car class", example = "Business")
        String carClass
) {
}
