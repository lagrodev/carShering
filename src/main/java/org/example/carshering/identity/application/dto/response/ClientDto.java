package org.example.carshering.identity.application.dto.response;

public record ClientDto(
        Long id,
        String firstName,
        String lastName,
        String login,
        String email,
        String phone,
        String password,
        Long roleId,
        boolean deleted,
        boolean banned,
        boolean emailVerified
        ) {
}
