package org.example.carshering.identity.api.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.api.dto.request.ResetPasswordRequest;
import org.example.carshering.identity.api.facade.ClientResponseFacade;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.identity.infrastructure.email.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Tag(name = "Working with mail", description = "Endpoints for sending email")
@RequestMapping("/api")
public class EmailController {

    private final EmailService emailService;
    private final ClientResponseFacade clientResponseFacade;
    private final ClientApplicationService clientService;

    @GetMapping("/verify")
    @Operation(
            summary = "Verify Email",
            description = "Verify user email using verification code sent to email"
    )
    @ApiResponse(
            responseCode = "302",
            description = "Email verified successfully, redirect to profile"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification code"
    )
    @Tag(name = "verify-email")
    @Tag(name = "Verify Email", description = "Verify user email using verification code sent to email")
    public ResponseEntity<Void> verifyToken(
            @Parameter(description = "Email verification code", example = "abc123xyz456")
            @RequestParam("code") String code
    ) {
        emailService.verifyToken(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Request Password Reset",
            description = "Initiate password reset process by sending reset email"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Email not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Email not verified"
    )
    @Tag(name = "request-password-reset")
    @Tag(name = "Request Password Reset", description = "Initiate password reset process by sending reset email")
    public ResponseEntity<?> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address for password reset",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResetPasswordRequest.class)
                    )
            )
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        var response = clientService.resetPasswordForEmail(request);
        return ResponseEntity.ok(response);
    }
}
