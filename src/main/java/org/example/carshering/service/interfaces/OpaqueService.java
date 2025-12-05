package org.example.carshering.service.interfaces;

import org.example.carshering.domain.entity.Client;
import org.example.carshering.security.ClientDetails;

public interface OpaqueService {

    String createOpaqueToken(ClientDetails user);

    Client getUserFromToken(String token);

    void invalidateAllTokensForUser(Client user);

    void invalidateToken(String refreshToken);
}
