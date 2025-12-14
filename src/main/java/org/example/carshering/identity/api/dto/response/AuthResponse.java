package org.example.carshering.identity.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Authentication response with tokens")
@Builder
public record AuthResponse (
        @Schema(description = "Token type", example = "Bearer")
        String type,
        @Schema(description = "Access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Refresh token for obtaining new access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {

}
