package org.example.carshering.fleet.api.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car image response")
public record ImageResponse(
        @Schema(description = "Image ID", example = "1")
        Long id,
        @Schema(description = "Image URL", example = "https://example.com/images/car-123.jpg")
        String url
) {
}
