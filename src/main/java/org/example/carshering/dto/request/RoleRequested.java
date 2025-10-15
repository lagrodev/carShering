package org.example.carshering.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleRequested(@NotBlank String RoleName) {

}
