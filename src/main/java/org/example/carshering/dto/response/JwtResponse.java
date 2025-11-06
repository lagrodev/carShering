package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication token response")
public record JwtResponse(
        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {
}
