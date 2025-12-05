package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.AuthRequest;
import org.example.carshering.dto.request.NewPasswordRequest;
import org.example.carshering.dto.request.RefreshTokenRequest;
import org.example.carshering.dto.response.AuthResponse;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.RefreshToken;
import org.example.carshering.domain.entity.VerificationCode;
import org.example.carshering.exceptions.custom.InvalidTokenException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CodeRepository;
import org.example.carshering.repository.RefreshTokenRepository;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ClientDetailsService clientDetailsService;
    private final AuthenticationManager authenticationManager;
    private final CodeRepository codeRepository;
    private final ClientService clientService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.username(), authRequest.password()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect login or password");
        } catch (LockedException e) {
            throw new LockedException("Account has been blocked");
        }
        ClientDetails userDetails = (ClientDetails) clientDetailsService.loadUserByUsername(authRequest.username());

        AuthResponse token = generateTokens(userDetails);

        // Устанавливаем cookie (HTTP-only, secure=false для dev, path=/api)


        return token;

    }
    private final OpaqueService opaqueService;
    private final JwtService jwtService;
    private AuthResponse generateTokens(ClientDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = opaqueService.createOpaqueToken(userDetails);
        return new AuthResponse("Bearer", accessToken, refreshToken);
    }




    @Override
    public void resetPassword(String code, NewPasswordRequest request) {
        VerificationCode verificationCode = codeRepository
                .findByCodeAndTypeIs(
                        code, VerificationCode.VerificationCodeType.PASSWORD_RESET)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        boolean isExpired = verificationCode.getCreatedAt()
                .plus(15, ChronoUnit.MINUTES)
                .isBefore(Instant.now());

        if (isExpired) {
            throw new RuntimeException("Time life token has passed");
        }

        Client client = verificationCode.getClient();

        clientService.updatePassword(client, request.password());
        codeRepository.delete(verificationCode); // TODO: use a service method for this
    }

    @Override
    public AuthResponse refreshAccessToken(RefreshTokenRequest tokenRequest) {

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenRequest.token())
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }


        Client client = opaqueService.getUserFromToken(tokenRequest.token());
        deleteRefreshToken(tokenRequest.token());

        return generateTokens((ClientDetails) clientDetailsService.loadUserByUsername(client.getLogin()));
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        opaqueService.invalidateToken(refreshToken);
    }


}
