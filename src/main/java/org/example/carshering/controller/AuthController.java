package org.example.carshering.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.JwtRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.JwtResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.AuthService;
import org.example.carshering.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    private final ClientService clientService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        return ResponseEntity.ok(new JwtResponse(authService.createAuthToken(authRequest)));
    }


    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(
            @Valid @RequestBody RegistrationRequest request
    ) {
        UserResponse userResponse = this.clientService.createUser(request);
        return ResponseEntity.ok(userResponse);
    }
}
