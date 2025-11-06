package org.example.carshering.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new car model")
public record CreateCarModelRequest(
        @Schema(description = "Brand of the car")
        @NotBlank String brand,
        @Schema(description = "Model of the car")
        @NotBlank String model,

        @Schema(description = "Body type of the car")
        @NotBlank String bodyType,

        @Schema(description = "Class of the car")
        @NotBlank String carClass
) {
}
