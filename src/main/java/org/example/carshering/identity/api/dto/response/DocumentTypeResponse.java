package org.example.carshering.identity.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Document type information response")
public record DocumentTypeResponse(
        @Schema(description = "Document type ID", example = "1")
        Long id,
        @Schema(description = "Document type name", example = "Passport")
        String name
) {
}
