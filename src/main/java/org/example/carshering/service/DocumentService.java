package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;

import java.util.List;


public interface DocumentService {
    DocumentResponse createDocument(@Valid CreateDocumentRequest createDocumentRequest, Long userId);
    boolean hasDocument(Long userId);
    DocumentResponse findDocument(Long userId);

    DocumentResponse updateDocument(Long userId, UpdateDocumentRequest request);


    void verifyDocument(Long documentId);

    List<DocumentResponse> getAllDocuments(boolean onlyUnverified);

    void deleteDocument(Long userId);
}
