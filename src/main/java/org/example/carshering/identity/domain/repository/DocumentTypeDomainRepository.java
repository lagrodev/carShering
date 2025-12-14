package org.example.carshering.identity.domain.repository;

import org.example.carshering.identity.domain.model.DocumentTypeModel;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;

import java.util.Optional;

public interface DocumentTypeDomainRepository {
    Optional<DocumentTypeModel> findById(DocumentTypeId id);
    DocumentTypeModel save(DocumentTypeModel type);
}
