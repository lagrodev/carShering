package org.example.carshering.identity.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to authenticate user")
public record AuthRequest(
        @Schema(description = "Username for authentication", example = "user101")
        @NotBlank String username,
        @Schema(description = "Password for authentication", example = "password")
        @NotBlank String password
        /*
        {
          "username": "user101",
          "password": "password"
        }
        */
) {
}
