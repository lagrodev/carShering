package org.example.carshering.dto.request;

public record JwtRequest(
        String username,
        String password
) {
}
