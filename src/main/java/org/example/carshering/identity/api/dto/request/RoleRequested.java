package org.example.carshering.identity.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update user role")
public record RoleRequested(
        @Schema(description = "Name of the role", example = "ADMIN")
        @NotBlank String RoleName
) {

}
