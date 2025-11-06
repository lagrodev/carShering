package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model name response")
public record ModelNameResponse(
        @Schema(description = "Name of the model", example = "Camry")
        String name
) {
}
