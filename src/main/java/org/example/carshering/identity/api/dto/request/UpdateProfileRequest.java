package org.example.carshering.identity.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update user profile")
public record UpdateProfileRequest (
        @Schema(description = "First name of the user", example = "John")
        String firstName,
        @Schema(description = "Last name of the user", example = "Doe")
        String lastName,
        @Schema(description = "Phone number", example = "+1234567890")
        String phone

){
}
