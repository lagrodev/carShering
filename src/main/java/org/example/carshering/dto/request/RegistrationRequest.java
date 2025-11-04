package org.example.carshering.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegistrationRequest (
        @NotBlank @Size(min = 6, max = 50) String login,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String lastName,
        @NotBlank @Email String email
){
}
