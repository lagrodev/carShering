package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.JwtService;
import org.example.carshering.utils.JwtTokenUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtTokenUtils jwtTokenUtils;
    @Override
    public String generateAccessToken(ClientDetails userDetails) {
        return jwtTokenUtils.generateToken(userDetails);
    }



}
