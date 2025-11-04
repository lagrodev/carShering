package org.example.carshering.service;

import org.example.carshering.dto.request.JwtRequest;

public interface AuthService {
    public String createAuthToken(JwtRequest authRequest);
}
