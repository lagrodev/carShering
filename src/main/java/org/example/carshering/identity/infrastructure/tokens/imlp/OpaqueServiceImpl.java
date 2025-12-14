package org.example.carshering.identity.infrastructure.tokens.imlp;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.identity.infrastructure.persistence.entity.RefreshToken;
import org.example.carshering.identity.infrastructure.persistence.repository.RefreshTokenRepository;
import org.example.carshering.identity.infrastructure.tokens.OpaqueService;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.utils.VerificationToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OpaqueServiceImpl implements OpaqueService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClientApplicationService clientService;

    @Override
    public String createOpaqueToken(ClientDetails user) {
        String token = VerificationToken.generateRefreshToken();


        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(token)
                .client(new ClientId(clientService.findUser(user.getId()).id()))
                .expiryDate(
                        Instant.ofEpochSecond(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                )


                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public ClientDto getUserFromToken(String token) {
        // todo подумать над хешированием
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(token)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);

        return clientService.findUser(refreshToken.getClient().value());
    }

    @Override
    public void invalidateAllTokensForUser(ClientJpaEntity user) {
        refreshTokenRepository.revokeAllByClient(user, Instant.now());

    }

    @Override
    @Transactional
    public void invalidateToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(refreshToken)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllByRevokedAtBefore(Instant.now().plusSeconds(30 * 24 * 60 * 60));
    }

}
