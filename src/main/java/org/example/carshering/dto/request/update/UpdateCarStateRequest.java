package org.example.carshering.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request to update car state")
public record UpdateCarStateRequest(
       @Schema(description = "Name of the car state", example = "AVAILABLE")
       @NotBlank String stateName
) {
}
