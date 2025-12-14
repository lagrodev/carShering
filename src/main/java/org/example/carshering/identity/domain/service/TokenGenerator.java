package org.example.carshering.identity.domain.service;

import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.identity.api.dto.response.AuthResponse;
import org.example.carshering.identity.domain.model.Client;

public interface  TokenGenerator {
    AuthResponse generateTokens(Client client);
    void invalidateRefreshToken(String token);
    ClientId getClientIdFromRefreshToken(String refreshToken);
}
