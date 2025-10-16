package org.example.carshering.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.CreateDocumentRequest;
import org.example.carshering.dto.request.UpdateProfileRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

//@RestController
@RequiredArgsConstructor
@RequestMapping("api/client")
public class UserController {
    private final ClientService clientService;
    private final DocumentService documentService;


    @GetMapping("/{userId}")
    public UserResponse getUserProfile(@PathVariable Long userId) {
        return clientService.findUser(userId);
    }

    @GetMapping("/{userId}/document")
    public DocumentResponse getUserDocument(@PathVariable Long userId) {
        return documentService.findDocument(userId);
    }


    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        this.clientService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }




    @PostMapping("/{userId}/document")
    public ResponseEntity<?> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            UriComponentsBuilder uriComponentsBuilder,
            @PathVariable Long userId) {

        DocumentResponse document = this.documentService.createDocument(request, userId);

        URI location = uriComponentsBuilder
                .path("/{userId}/document/{documentId}")

                .build(Map.of(
                        "userId", userId,
                        "documentId", document.id()
                ));

        return ResponseEntity.created(location).body(document);
    }


}
