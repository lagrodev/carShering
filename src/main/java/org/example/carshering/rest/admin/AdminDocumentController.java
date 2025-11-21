package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.service.interfaces.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Admin Document Management", description = "Endpoints for admin document management")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDocumentController {
    //todo все
    private final DocumentService documentService;

    // Все документы
    @GetMapping("/documents")
    @Operation(
            summary = "Get All Documents",
            description = "Retrieve a paginated list of documents with optional filtering for unverified documents"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of documents retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocumentResponse.class)
            )
    )
    @Tag(name = "get-documents")
    @Tag(name = "Get Documents", description = "Retrieve a paginated list of documents with optional filtering for unverified documents")
    public Page<DocumentResponse> getAllDocuments(
            @Parameter(description = "Show only unverified documents", example = "true")
            @RequestParam(defaultValue = "true") boolean onlyUnverified,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return documentService.getAllDocuments(onlyUnverified, pageable);
    }

    // Подтвердить документ
    @PatchMapping("/documents/{documentId}/verify")
    @Operation(
            summary = "Verify Document",
            description = "Verify a document by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Document verified successfully"
    )
    @Tag(name = "verify-document")
    @Tag(name = "Verify Document", description = "Verify a document by its ID")
    public ResponseEntity<?> verifyDocument(
            @Parameter(description = "ID of the document to verify", example = "1")
            @PathVariable Long documentId
    ) {
        documentService.verifyDocument(documentId);
        return ResponseEntity.ok().build();
    }


}