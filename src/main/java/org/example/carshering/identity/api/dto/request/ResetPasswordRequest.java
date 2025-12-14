package org.example.carshering.identity.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to reset user password")
public record ResetPasswordRequest(
        @Schema(description = "Email address of the user", example = "user@example.com")
        @NotBlank String email
)
{
}
