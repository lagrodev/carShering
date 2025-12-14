//package org.example.carshering.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.api.dto.response.DocumentTypeResponse;
//import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
//import org.example.carshering.common.exceptions.custom.DocumentTypeException;
//import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
//import org.example.carshering.service.interfaces.DocumentTypeService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class DocumentTypeServiceImpl implements DocumentTypeService {
//    private final DocumentTypeRepository repository;
//
//    @Override
//    public List<DocumentTypeResponse> getAllTypes() {
//        return repository.findAll().stream()
//                .map(type -> new DocumentTypeResponse(type.getId(), type.getName()))
//                .toList();
//    }
//
//    @Override
//    public DocumentType getById(Long id) {
//        return repository.findById(id)
//                .orElseThrow(() -> new DocumentTypeException("Document type not found"));
//    }
//}
