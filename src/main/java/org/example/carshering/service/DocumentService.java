package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface DocumentService {
    DocumentResponse createDocument(@Valid CreateDocumentRequest createDocumentRequest, Long userId);
    boolean hasDocument(Long userId);
    DocumentResponse findDocument(Long userId);

    DocumentResponse updateDocument(Long userId, UpdateDocumentRequest request);


    void verifyDocument(Long documentId);

    Page<DocumentResponse> getAllDocuments(boolean onlyUnverified, Pageable pageable);

    void deleteDocument(Long userId);
}
