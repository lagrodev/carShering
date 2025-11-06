package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.JwtRequest;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.AuthService;
import org.example.carshering.service.ClientDetailsService;
import org.example.carshering.service.ClientService;
import org.example.carshering.utils.JwtTokenUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenUtils jwtTokenUtils;
    private final ClientDetailsService clientDetailsService;
    private final AuthenticationManager authenticationManager;

    @Override
    public String createAuthToken(JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.username(), authRequest.password()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect login or password");
        } catch (LockedException e) {
            throw new LockedException("Account has been blocked");
        }
        ClientDetails userDetails = (ClientDetails) clientDetailsService.loadUserByUsername(authRequest.username());
        return jwtTokenUtils.generateToken(userDetails);

    }

    // todo refresh token

}
