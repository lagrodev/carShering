package org.example.carshering.service.interfaces;

import org.example.carshering.identity.api.dto.response.DocumentTypeResponse;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;

import java.util.List;


public interface DocumentTypeService {
    List<DocumentTypeResponse> getAllTypes();
    DocumentType getById(Long id);
}
