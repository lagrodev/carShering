package org.example.carshering.identity.infrastructure.tokens;

import org.example.carshering.security.ClientDetails;

public interface JwtService {
    String generateAccessToken(ClientDetails userDetails);
}
