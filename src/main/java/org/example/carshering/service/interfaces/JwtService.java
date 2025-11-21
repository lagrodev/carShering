package org.example.carshering.service.interfaces;

import org.example.carshering.security.ClientDetails;

public interface JwtService {
    String generateAccessToken(ClientDetails userDetails);
}
