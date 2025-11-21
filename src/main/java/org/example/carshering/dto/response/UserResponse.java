package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "User information response")
public record UserResponse(
   @Schema(description = "User ID", example = "1")
   Long id,
   @Schema(description = "First name", example = "John")
   String firstName,
   @Schema(description = "Last name", example = "Doe")
   String lastName,
   @Schema(description = "Login username", example = "johndoe")
   String login,
   @Schema(description = "Phone number", example = "+1234567890")
   String phone,
   @Schema(description = "Email address", example = "john@example.com")
   String email,
   @Schema(description = "Email verification status", example = "true")
   boolean emailVerified

) {}
