package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short user information response")
public record ShortUserResponse(
   @Schema(description = "User ID", example = "1")
   Long id,
   @Schema(description = "Login username", example = "johndoe")
   String login,
   @Schema(description = "Email address", example = "john@example.com")
   String email,
   @Schema(description = "Role name", example = "USER")
   String roleName,
   @Schema(description = "Banned status", example = "false")
   boolean banned,
   @Schema(description = "Email verification status", example = "true")
   boolean emailVerified
) {}
