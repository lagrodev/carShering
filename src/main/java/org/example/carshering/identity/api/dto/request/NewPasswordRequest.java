package org.example.carshering.identity.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to set a new password")
public record NewPasswordRequest(
        @Schema(description = "New password for the user", example = "NewSecurePass123!")
        String password
) {
}
