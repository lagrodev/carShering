package org.example.carshering.rest.all;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateProfileRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ClientService;
import org.example.carshering.service.interfaces.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.core.Authentication;
import java.net.URI;
import java.util.Map;

@RestController
@Tag(name = "User Profile", description = "Endpoints for user profile and document management")
@RequestMapping("api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;
    private final DocumentService documentService;

    @GetMapping
    @Operation(
            summary = "Get Profile",
            description = "Retrieve the profile of the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "204",
            description = "No profile found"
    )
    @Tag(name = "get-profile")
    @Tag(name = "Get Profile", description = "Retrieve the profile of the authenticated user")
    public ResponseEntity<?> getProfile(
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        UserResponse user = clientService.findUser(userId);

        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(user);

    }

    @GetMapping("/me")
    @Operation(
            summary = "Get Me",
            description = "Retrieve basic information about the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User information retrieved successfully"
    )
    @ApiResponse(
            responseCode = "204",
            description = "No user found"
    )
    @Tag(name = "get-me")
    @Tag(name = "Get Me", description = "Retrieve basic information about the authenticated user")
    public ResponseEntity<?> me(@AuthenticationPrincipal ClientDetails user) {
        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "authorities", user.getAuthorities()
        ));
    }




    @DeleteMapping
    @Operation(
            summary = "Delete Profile",
            description = "Delete the profile of the authenticated user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Profile deleted successfully"
    )
    @Tag(name = "delete-profile")
    @Tag(name = "Delete Profile", description = "Delete the profile of the authenticated user")
    public  ResponseEntity<?> deleteProfile(
            Authentication auth
    ){
        Long userId = getCurrentUserId(auth);
        clientService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/password")
    @Operation(
            summary = "Change Password",
            description = "Change the password of the authenticated user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Password changed successfully"
    )
    @Tag(name = "change-password")
    @Tag(name = "Change Password", description = "Change the password of the authenticated user")
    public ResponseEntity<?> changePassword(
            Authentication auth,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChangePasswordRequest.class)
                    )
            )
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Long userId = getCurrentUserId(auth);
        clientService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/document")
    @Operation(
            summary = "Get Document",
            description = "Retrieve the document of the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Document retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocumentResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "204",
            description = "No document found"
    )
    @Tag(name = "get-document")
    @Tag(name = "Get Document", description = "Retrieve the document of the authenticated user")
    public ResponseEntity<?> getDocument(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        DocumentResponse doc = documentService.findDocument(userId);
        if (doc == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(doc);
    }


    @PostMapping("/document")
    @Operation(
            summary = "Create Document",
            description = "Create a new document for the authenticated user"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Document created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocumentResponse.class)
            )
    )
    @Tag(name = "create-document")
    @Tag(name = "Create Document", description = "Create a new document for the authenticated user")
    public ResponseEntity<?> createDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Document details to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateDocumentRequest.class)
                    )
            )
            @Valid @RequestBody CreateDocumentRequest request,
            Authentication auth,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        Long userId = getCurrentUserId(auth);
        DocumentResponse doc = documentService.createDocument(request, userId);
        URI location = uriComponentsBuilder.path("/api/profile/document").build().toUri();
        return ResponseEntity.created(location).body(doc);
    }



    @PatchMapping("/document")
    @Operation(
            summary = "Update Document",
            description = "Update the document of the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Document updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocumentResponse.class)
            )
    )
    @Tag(name = "update-document")
    @Tag(name = "Update Document", description = "Update the document of the authenticated user")
    public ResponseEntity<?> updateDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated document details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateDocumentRequest.class)
                    )
            )
            @Valid @RequestBody UpdateDocumentRequest request,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        DocumentResponse doc = documentService.updateDocument(userId, request);
        return ResponseEntity.ok(doc);
    }

    @DeleteMapping("/document")
    @Operation(
            summary = "Delete Document",
            description = "Delete the document of the authenticated user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Document deleted successfully"
    )
    @Tag(name = "delete-document")
    @Tag(name = "Delete Document", description = "Delete the document of the authenticated user")
    public ResponseEntity<?> deleteDocument(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        documentService.deleteDocument(userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping
    @Operation(
            summary = "Update Profile",
            description = "Update the profile information of the authenticated user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Profile updated successfully"
    )
    @Tag(name = "update-profile")
    @Tag(name = "Update Profile", description = "Update the profile information of the authenticated user")
    public ResponseEntity<?> updateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated profile details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateProfileRequest.class)
                    )
            )
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        clientService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/verify")
    @Operation(
            summary = "Verify email user",
            description = "Send latter to Verify email user of the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Profile send letter to email successfully"
    )
    @Tag(name = "verify-email")
    @Tag(name = "Verify email",  description = "Verify email user of the authenticated user")
    public ResponseEntity<?> verifyEmail(
            Authentication auth
            ) {
        Long userId = getCurrentUserId(auth);
        return ResponseEntity.ok(clientService.verifyEmail(userId));
    }




    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }
}