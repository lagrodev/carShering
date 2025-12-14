package org.example.carshering.identity.api.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.api.dto.response.DocumentResponse;
import org.example.carshering.identity.api.facade.ClientResponseFacade;
import org.example.carshering.identity.application.service.ClientApplicationService;
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

    private final ClientResponseFacade clientResponseFacade;
    private final ClientApplicationService documentService;


    // Все документы
    @GetMapping("/documents")
    @Operation(
            summary = "Get All Documents",
            description = "Retrieve a paginated list of documents with optional filtering for unverified documents (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of documents retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    public Page<DocumentResponse> getAllDocuments(
            @Parameter(description = "Show only unverified documents", example = "true")
            @RequestParam(defaultValue = "true") boolean onlyUnverified,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return documentService.getAllDocuments(onlyUnverified, pageable).map(clientResponseFacade::getDocumentResponse);
    }

    // Подтвердить документ
    @PatchMapping("/documents/{documentId}/verify")
    @Operation(
            summary = "Verify Document",
            description = "Verify a document by its ID (admin access)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Document verified successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Document not found"
    )
    public ResponseEntity<?> verifyDocument(
            @Parameter(description = "ID of the document to verify", example = "1", required = true)
            @PathVariable Long documentId
    ) {
        documentService.verifyDocument(documentId);
        return ResponseEntity.ok().build();
    }


}