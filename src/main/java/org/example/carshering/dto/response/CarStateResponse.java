package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Car state information response")
public record CarStateResponse(
        @Schema(description = "Car state ID", example = "1")
        Long id,
        @Schema(description = "Car status", example = "AVAILABLE")
        String status
) {
}
