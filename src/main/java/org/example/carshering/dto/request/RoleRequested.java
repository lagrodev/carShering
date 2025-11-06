package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update user role")
public record RoleRequested(
        @Schema(description = "Name of the role", example = "ADMIN")
        @NotBlank String RoleName
) {

}
