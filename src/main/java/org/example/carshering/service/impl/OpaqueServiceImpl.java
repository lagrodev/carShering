package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.RefreshToken;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.RefreshTokenRepository;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ClientService;
import org.example.carshering.service.interfaces.OpaqueService;
import org.example.carshering.utils.VerificationToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OpaqueServiceImpl implements OpaqueService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClientService clientService;

    @Override
    public String createOpaqueToken(ClientDetails user) {
        String token = VerificationToken.generateRefreshToken();


        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(token)
                .client(clientService.getEntity(user.getId()))
                .expiryDate(
                        Instant.ofEpochSecond(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                )


                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public Client getUserFromToken(String token) {
        // todo подумать над хешированием
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(token)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getClient();
    }

    @Override
    public void invalidateAllTokensForUser(Client user) {
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
