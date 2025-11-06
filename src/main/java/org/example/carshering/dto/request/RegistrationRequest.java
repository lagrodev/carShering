package org.example.carshering.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request to register a new user")
public record RegistrationRequest (
        @Schema(description = "Login username", example = "user123", minLength = 6, maxLength = 50)
        @NotBlank @Size(min = 6, max = 50) String login,
        @Schema(description = "User password", example = "password123", minLength = 6)
        @NotBlank @Size(min = 6) String password,
        @Schema(description = "Last name of the user", example = "Smith")
        @NotBlank String lastName,
        @Schema(description = "Email address", example = "user@example.com")
        @NotBlank @Email String email
){
}
