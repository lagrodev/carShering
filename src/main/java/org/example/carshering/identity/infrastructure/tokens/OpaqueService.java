package org.example.carshering.identity.infrastructure.tokens;

import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.security.ClientDetails;

public interface OpaqueService {

    String createOpaqueToken(ClientDetails user);

    ClientDto getUserFromToken(String token);


    void invalidateAllTokensForUser(ClientJpaEntity user);

    void invalidateToken(String refreshToken);
}
