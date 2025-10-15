package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.JwtRequest;
import org.example.carshering.dto.response.JwtResponse;
import org.example.carshering.exceptions.AppError;
import org.example.carshering.security.ClientDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    public String createAuthToken(JwtRequest authRequest);
}
