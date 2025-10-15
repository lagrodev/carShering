package org.example.carshering.service;

import org.example.carshering.dto.response.DocumentTypeResponse;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.repository.DocumentTypeRepository;

import java.util.List;


public interface DocumentTypeService {
    List<DocumentTypeResponse> getAllTypes();
    DocumentType getById(Long id);
}
