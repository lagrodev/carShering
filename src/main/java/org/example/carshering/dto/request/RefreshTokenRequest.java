package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to refresh access token")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {
}
