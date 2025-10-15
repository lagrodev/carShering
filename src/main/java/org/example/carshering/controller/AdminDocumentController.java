package org.example.carshering.controller;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentService documentService;

    // Все документы
    @GetMapping("/documents")
    public List<DocumentResponse> getAllDocuments(
            @RequestParam(defaultValue = "true") boolean onlyUnverified
    ) {
        return documentService.getAllDocuments(onlyUnverified);
    }

    // Подтвердить документ
    @PatchMapping("/documents/{documentId}/verify")
    public ResponseEntity<?> verifyDocument(@PathVariable Long documentId) {
        documentService.verifyDocument(documentId);
        return ResponseEntity.noContent().build();
    }


}