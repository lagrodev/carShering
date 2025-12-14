//package org.example.carshering.service.interfaces;
//
//import jakarta.validation.Valid;
//import org.example.carshering.identity.api.dto.request.CreateDocumentRequest;
//import org.example.carshering.identity.api.dto.request.UpdateDocumentRequest;
//import org.example.carshering.identity.api.dto.response.DocumentResponse;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//
//public interface DocumentService {
//    DocumentResponse createDocument(@Valid CreateDocumentRequest createDocumentRequest, Long userId);
//    boolean hasDocument(Long userId);
//    DocumentResponse findDocument(Long userId);
//
//    DocumentResponse updateDocument(Long userId, UpdateDocumentRequest request);
//
//
//    void verifyDocument(Long documentId);
//
//    Page<DocumentResponse> getAllDocuments(boolean onlyUnverified, Pageable pageable);
//
//    void deleteDocument(Long userId);
//}
