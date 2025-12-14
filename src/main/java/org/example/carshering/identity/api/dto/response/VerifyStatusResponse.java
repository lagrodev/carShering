package org.example.carshering.identity.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email verification status response")
public record VerifyStatusResponse(
        @Schema(description = "Verification status", example = "success")
        String status,
        @Schema(description = "Status message", example = "Email verified successfully")
        String message
) {
}
