//package org.example.carshering.service.domain;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
//import org.example.carshering.common.exceptions.custom.DocumentTypeException;
//import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class DocumentTypeServiceHelper {
//    private final DocumentTypeRepository repository;
//
//    public DocumentType getById(Long id) {
//        return repository.findById(id)
//                .orElseThrow(() -> new DocumentTypeException("Document type not found"));
//    }
//}
