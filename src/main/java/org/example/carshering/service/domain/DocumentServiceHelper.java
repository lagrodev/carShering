//package org.example.carshering.service.domain;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.api.dto.response.DocumentResponse;
//import org.example.carshering.identity.api.mapper.DocumentMapper;
//import org.example.carshering.identity.infrastructure.persistence.repository.DocumentRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class DocumentServiceHelper {
//
//    public boolean hasDocument(Long userId) {
//        return documentRepository.existsByClientIdAndDeletedFalse(userId);
//    }
//    private final DocumentMapper documentMapper;
//    private final DocumentRepository documentRepository;
//
//    public DocumentResponse findDocument(Long userId) {
//        return documentRepository.findByClientIdAndDeletedFalse(userId)
//                .map(documentJpaEntity ->
//                        documentMapper.toDto(documentJpaEntity))
//                .orElse(null);
//    }
//}
