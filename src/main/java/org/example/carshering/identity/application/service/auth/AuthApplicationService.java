package org.example.carshering.identity.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.identity.api.dto.request.RefreshTokenRequest;
import org.example.carshering.identity.api.dto.request.AuthRequest;
import org.example.carshering.identity.api.dto.response.AuthResponse;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.repository.ClientDomainRepository;
import org.example.carshering.identity.domain.service.TokenGenerator;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {
    private final ClientDomainRepository clientRepository;
    private final TokenGenerator tokenGenerator;

    public AuthResponse login(AuthRequest request) {
        Client client = clientRepository.findByLogin(Login.of(request.username()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!client.getPassword().matches(request.password())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (client.isBanned()) {
            throw new LockedException("Account has been blocked");
        }

        if (client.isDeleted()) {
            throw new BadCredentialsException("Account not found");
        }

        return tokenGenerator.generateTokens(client);
    }

    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        // Получаем clientId из refresh токена
        ClientId clientId = tokenGenerator.getClientIdFromRefreshToken(request.token());

        // Загружаем клиента из репозитория
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BadCredentialsException("Client not found"));

        // Проверяем статус клиента
        if (client.isBanned()) {
            throw new LockedException("Account has been blocked");
        }

        if (client.isDeleted()) {
            throw new BadCredentialsException("Account not found");
        }

        // Инвалидируем старый refresh токен
        tokenGenerator.invalidateRefreshToken(request.token());

        // Генерируем новую пару токенов
        return tokenGenerator.generateTokens(client);
    }

    public void logout(String refreshToken) {
        tokenGenerator.invalidateRefreshToken(refreshToken);
    }
}