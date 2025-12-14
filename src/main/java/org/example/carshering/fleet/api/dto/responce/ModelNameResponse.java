package org.example.carshering.fleet.api.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model name response")
public record ModelNameResponse(
        @Schema(description = "Model name ID", example = "1")
        Long id,
        @Schema(description = "Name of the model", example = "Camry")
        String name
) {
}
