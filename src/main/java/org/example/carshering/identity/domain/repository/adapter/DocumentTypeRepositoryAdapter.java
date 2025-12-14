package org.example.carshering.identity.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.domain.model.DocumentTypeModel;
import org.example.carshering.identity.domain.repository.DocumentTypeDomainRepository;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
import org.example.carshering.identity.infrastructure.persistence.mapper.DocumentTypeMapper;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DocumentTypeRepositoryAdapter implements DocumentTypeDomainRepository {
    private final DocumentTypeMapper documentTypeMapper;
    private final DocumentTypeRepository documentTypeRepository;
    @Override
    public Optional<DocumentTypeModel> findById(DocumentTypeId id) {
        Optional<DocumentType> documentType = documentTypeRepository.findById(id.value());
        return documentType.map(documentTypeMapper::toDomain);
    }

    @Override
    public DocumentTypeModel save(DocumentTypeModel type) {
        DocumentType entity = documentTypeMapper.toEntity(type);
        DocumentType savedEntity = documentTypeRepository.save(entity);
        return documentTypeMapper.toDomain(savedEntity);
    }
}
