package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for password reset request")
public record ResetPasswordResponse(
        @Schema(description = "Status of the password reset request", example = "success")
        String status,
        @Schema(description = "Message describing the result", example = "Password reset link sent to your email")
        String message
) {
}
