package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.AuthRequest;
import org.example.carshering.dto.request.NewPasswordRequest;
import org.example.carshering.dto.request.RefreshTokenRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.AuthResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.interfaces.AuthService;
import org.example.carshering.service.interfaces.ClientService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Tag(name = "login")
    @Tag(name = "login", description = "Login user and create auth token")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Authentication credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthRequest.class)
                    )
            )
            @RequestBody @Valid AuthRequest authRequest,
            HttpServletResponse response
    ) {
        // todo: обновить логику, ибо пока мне не нравится, что он перезаписывает, к примеру - в куки устанавливает время, но оно уже есть в jwt токене - зачем? лучше тогда, чтобы он просто отправлял в локалке, в куки он уже на стороне фронта засовывал?
        AuthResponse tokens = authService.login(authRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookie("access_token", tokens.accessToken(), 30 * 60).toString())
                .header(HttpHeaders.SET_COOKIE, buildCookie("refresh_token", tokens.refreshToken(), 7 * 24 * 60 * 60).toString())
                .body(Map.of("message", "Login successful"));
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
    public ResponseEntity<?> logout(
            HttpServletResponse response,
            @Parameter(description = "Refresh token from cookie", required = false)
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.deleteRefreshToken(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("access_token").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie("refresh_token").toString());

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }


    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Token",
            description = "Refresh access token using refresh token from cookies"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing refresh token"
    )
    @Tag(name = "refresh-token")
    @Tag(name = "Refresh Token", description = "Refresh access token using refresh token from cookies")
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "Refresh token from cookie", required = true)
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearCookie("access_token").toString())
                    .header(HttpHeaders.SET_COOKIE, clearCookie("refresh_token").toString())
                    .body(Map.of("error", "Invalid refresh token"));
        }


        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        AuthResponse tokens = authService.refreshAccessToken(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookie("access_token", tokens.accessToken(), 30 * 60).toString())
                .header(HttpHeaders.SET_COOKIE, buildCookie("refresh_token", tokens.refreshToken(), 7 * 24 * 60 * 60).toString())
                .body(Map.of("message", "Token refreshed successfully"));
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie buildCookie(String name, String value, int maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // например, по профилю
                .path("/api")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/reset")
    @Operation(
            summary = "Reset Password",
            description = "Reset user password using reset code sent to email"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Password reset successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired reset code"
    )
    @Tag(name = "reset-password")
    @Tag(name = "Reset Password", description = "Reset user password using reset code sent to email")
    public ResponseEntity<Void> resetPasswordToken(
            @Parameter(description = "Password reset code", example = "reset123xyz456")
            @RequestParam("code") String code,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New password details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegistrationRequest.class)
                    )
            )
            @RequestBody NewPasswordRequest request
    ) {
        authService.resetPassword(code, request);
        return ResponseEntity.ok().build();
    }


}
