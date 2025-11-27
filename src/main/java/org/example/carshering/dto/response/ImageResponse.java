package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car image response")
public record ImageResponse(
        @Schema(description = "Image ID", example = "1")
        Long id,
        @Schema(description = "Image URL", example = "https://example.com/images/car-123.jpg")
        String url
) {
}
