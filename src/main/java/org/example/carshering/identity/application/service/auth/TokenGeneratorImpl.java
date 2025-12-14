package org.example.carshering.identity.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.identity.api.dto.response.AuthResponse;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.service.TokenGenerator;
import org.example.carshering.identity.infrastructure.tokens.JwtService;
import org.example.carshering.identity.infrastructure.tokens.OpaqueService;
import org.example.carshering.security.ClientDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenGeneratorImpl implements TokenGenerator {
    private final JwtService jwtService;
    private final OpaqueService opaqueService;
    private final ClientDetailsService clientDetailsService;

    @Override
    public AuthResponse generateTokens(Client client) {
        ClientDetails userDetails = (ClientDetails) clientDetailsService.loadUserByUsername(client.getLogin().getValue());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = opaqueService.createOpaqueToken(userDetails);
        return new AuthResponse("Bearer", accessToken, refreshToken);
    }

    @Override
    public void invalidateRefreshToken(String token) {
        opaqueService.invalidateToken(token);
    }

    @Override
    public ClientId getClientIdFromRefreshToken(String refreshToken) {
        ClientDto client = opaqueService.getUserFromToken(refreshToken);
        return new ClientId(client.id());
    }
}
