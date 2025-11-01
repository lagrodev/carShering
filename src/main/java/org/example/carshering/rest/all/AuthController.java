package org.example.carshering.rest.all;

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
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    private final ClientService clientService;

    //@PostMapping("/auth2")
    public ResponseEntity<?> createAuthToken2(@RequestBody JwtRequest authRequest) {
        return ResponseEntity.ok(new JwtResponse(authService.createAuthToken(authRequest)));
    }



    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(
            @RequestBody JwtRequest authRequest,
//            @Value("${jwt.lifetime}") Duration jwtLifetime,
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
    public ResponseEntity<?> createNewUser(
            @Valid @RequestBody RegistrationRequest request
    ) {
        //System.out.println(request);
        UserResponse userResponse = this.clientService.createUser(request);
        return ResponseEntity.ok(userResponse);
    }


    @PostMapping("/logout")
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
