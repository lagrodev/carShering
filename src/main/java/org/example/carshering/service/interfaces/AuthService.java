package org.example.carshering.service.interfaces;

import org.example.carshering.dto.request.AuthRequest;
import org.example.carshering.dto.request.NewPasswordRequest;
import org.example.carshering.dto.request.RefreshTokenRequest;
import org.example.carshering.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    void verifyToken(String code);

    void resetPassword(String code, NewPasswordRequest request);

    AuthResponse refreshAccessToken(RefreshTokenRequest refreshToken);

    void deleteRefreshToken(String refreshToken);

}
