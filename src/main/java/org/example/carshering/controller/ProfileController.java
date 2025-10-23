package org.example.carshering.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.CreateDocumentRequest;
import org.example.carshering.dto.request.UpdateDocumentRequest;
import org.example.carshering.dto.request.UpdateProfileRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.core.Authentication;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;
    private final DocumentService documentService;

    @GetMapping
    public UserResponse getProfile(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return clientService.findUser(userId);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal ClientDetails user) {
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "authorities", user.getAuthorities()
        ));
    }




    @DeleteMapping
    public  ResponseEntity<?> deleteProfile(
            Authentication auth
    ){
        Long userId = getCurrentUserId(auth);
        clientService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Long userId = getCurrentUserId(auth);
        clientService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/document")
    public DocumentResponse getDocument(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        return documentService.findDocument(userId);
    }


    @PostMapping("/document")
    public ResponseEntity<?> createDocument(
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
    public ResponseEntity<?> updateDocument(
            @Valid @RequestBody UpdateDocumentRequest request,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        DocumentResponse doc = documentService.updateDocument(userId, request);
        return ResponseEntity.ok(doc);
    }

    @DeleteMapping("/document")
    public ResponseEntity<?> deleteDocument(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        documentService.deleteDocument(userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication auth
    ) {
        Long userId = getCurrentUserId(auth);
        clientService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }





    private Long getCurrentUserId(Authentication auth) {
        return ((ClientDetails) auth.getPrincipal()).getId();
    }
}