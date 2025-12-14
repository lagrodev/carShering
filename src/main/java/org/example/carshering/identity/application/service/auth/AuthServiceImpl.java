//package org.example.carshering.identity.application.service.auth;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.common.exceptions.custom.InvalidTokenException;
//import org.example.carshering.common.exceptions.custom.NotFoundException;
//import org.example.carshering.identity.application.dto.response.ClientDto;
//import org.example.carshering.identity.infrastructure.persistence.entity.RefreshToken;
//import org.example.carshering.identity.api.dto.request.RefreshTokenRequest;
//import org.example.carshering.identity.api.dto.request.AuthRequest;
//import org.example.carshering.identity.api.dto.response.AuthResponse;
//import org.example.carshering.identity.infrastructure.tokens.JwtService;
//import org.example.carshering.identity.infrastructure.tokens.OpaqueService;
//import org.example.carshering.identity.infrastructure.persistence.repository.RefreshTokenRepository;
//import org.example.carshering.security.ClientDetails;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.LockedException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//
//@Service
//@RequiredArgsConstructor
//public class AuthServiceImpl implements AuthService {
//
//    private final ClientDetailsService clientDetailsService;
//    private final AuthenticationManager authenticationManager;
//
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final OpaqueService opaqueService;
//    private final JwtService jwtService;
//
//    @Override
//    public AuthResponse login(AuthRequest authRequest) {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                    authRequest.username(), authRequest.password()));
//        } catch (BadCredentialsException e) {
//            throw new BadCredentialsException("Incorrect login or password");
//        } catch (LockedException e) {
//            throw new LockedException("Account has been blocked");
//        }
//        ClientDetails userDetails = (ClientDetails) clientDetailsService.loadUserByUsername(authRequest.username());
//
//        AuthResponse token = generateTokens(userDetails);
//
//        // Устанавливаем cookie (HTTP-only, secure=false для dev, path=/api)
//
//
//        return token;
//
//    }
//
//    private AuthResponse generateTokens(ClientDetails userDetails) {
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        String refreshToken = opaqueService.createOpaqueToken(userDetails);
//        return new AuthResponse("Bearer", accessToken, refreshToken);
//    }
//
//
//
//    @Override
//    public AuthResponse refreshAccessToken(RefreshTokenRequest tokenRequest) {
//
//        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenRequest.token())
//                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
//
//        if (refreshToken.isRevoked()) {
//            throw new InvalidTokenException("Refresh token has been revoked");
//        }
//        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
//            throw new InvalidTokenException("Refresh token has expired");
//        }
//
//
//        ClientDto client = opaqueService.getUserFromToken(tokenRequest.token());
//        deleteRefreshToken(tokenRequest.token());
//
//        return generateTokens((ClientDetails) clientDetailsService.loadUserByUsername(client.login()));
//    }
//
//    @Override
//    public void deleteRefreshToken(String refreshToken) {
//        opaqueService.invalidateToken(refreshToken);
//    }
//
//
//}
