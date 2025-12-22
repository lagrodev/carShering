package org.example.carshering.identity.infrastructure.persistence.mapper;

import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentJpaEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapperForJpa {

    // Domain -> JPA Entity
    @Mapping(source = "documentId.value", target = "id")
    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "documentSeries", target = "series")
    @Mapping(source = "documentNumber", target = "number")
    @Mapping(source = "dateOfIssue", target = "dateOfIssue")
    @Mapping(source = "issuingAuthority", target = "issuingAuthority")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "deleted", target = "deleted")
    @Mapping(target = "clientId", ignore = true) // Устанавливается в Repository!
    DocumentJpaEntity toEntity(Document document);

    // JPA Entity -> Domain
    default Document toDomain(DocumentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Document.restore(
            entity.getId() != null ? new DocumentId(entity.getId()) : null,
            entity.getDocumentType(),
            entity.getSeries(),
            entity.getNumber(),
            entity.getDateOfIssue(),
            entity.getIssuingAuthority(),
            entity.isVerified(),
            entity.isDeleted()
        );
    }

    default void updateEntity(DocumentJpaEntity entity, Document document) {
        // Immutable поля не обновляются!
        entity.setDateOfIssue(document.getDateOfIssue());
        entity.setIssuingAuthority(document.getIssuingAuthority());
        entity.setVerified(document.isVerified());
        entity.setDeleted(document.isDeleted());
    }

}
