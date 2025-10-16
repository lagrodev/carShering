package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.CreateDocumentRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;

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
