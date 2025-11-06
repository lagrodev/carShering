package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car model information response")
public record CarModelResponse(
        @Schema(description = "Model ID", example = "1")
        Long modelId,
        @Schema(description = "Brand name", example = "Toyota")
        String brand,
        @Schema(description = "Model name", example = "Camry")
        String model,
        @Schema(description = "Body type", example = "Sedan")
        String bodyType,
        @Schema(description = "Car class", example = "Business")
        String carClass,
        @Schema(description = "Is deleted flag", example = "false")
        boolean isDeleted
) {
}
