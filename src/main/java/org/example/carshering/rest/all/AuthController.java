package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.JwtRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.JwtResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.AuthService;
import org.example.carshering.service.ClientService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    private final ClientService clientService;


    @PostMapping("/auth")
    @Operation(
            summary = "Authenticate",
            description = "Authenticate user and create auth token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Authentication successful"
    )
    @Tag(name = "authenticate")
    @Tag(name = "Authenticate", description = "Authenticate user and create auth token")
    public ResponseEntity<?> createAuthToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Authentication credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = JwtRequest.class)
                    )
            )
            @RequestBody @Valid JwtRequest authRequest,
            HttpServletResponse response) {
        String token = authService.createAuthToken(authRequest);

        // Устанавливаем cookie (HTTP-only, secure=false для dev, path=/api)
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(false) // true в продакшене (только HTTPS)
                .path("/api")  // cookie будет отправляться только к /api/**
                .maxAge(30*60) //jwtLifetime.toMillis() 30 минут в секундах
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Authentication successful"));
    }


    @PostMapping("/registration")
    @Operation(
            summary = "Register",
            description = "Register a new user in the system"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            )
    )
    @Tag(name = "register")
    @Tag(name = "Register", description = "Register a new user in the system")
    public ResponseEntity<?> createNewUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegistrationRequest.class)
                    )
            )
            @Valid @RequestBody RegistrationRequest request
    ) {
        //System.out.println(request);
        UserResponse userResponse = this.clientService.createUser(request);
        return ResponseEntity.ok(userResponse);
    }


    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Logout user and clear authentication token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Logged out successfully"
    )
    @Tag(name = "logout")
    @Tag(name = "Logout", description = "Logout user and clear authentication token")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api")
                .maxAge(0) // удаляет cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(Map.of("message", "Logged out"));
    }

}
