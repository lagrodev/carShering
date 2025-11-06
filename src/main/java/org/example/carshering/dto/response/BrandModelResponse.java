package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Brand model response")
public record BrandModelResponse(
        @Schema(description = "Brand name", example = "Toyota")
        String brand
) {
}
