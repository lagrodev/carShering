package org.example.carshering.service.interfaces;

import org.example.carshering.dto.response.DocumentTypeResponse;
import org.example.carshering.entity.DocumentType;

import java.util.List;


public interface DocumentTypeService {
    List<DocumentTypeResponse> getAllTypes();
    DocumentType getById(Long id);
}
