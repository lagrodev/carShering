package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to change user password")
public record ChangePasswordRequest(
        @Schema(description = "Current password", example = "oldPassword123")
        String oldPassword,
        @Schema(description = "New password", example = "newPassword123")
        String newPassword
) {}
