package org.example.carshering.identity.infrastructure.persistence.mapper;

import org.example.carshering.identity.domain.model.DocumentTypeModel;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentTypeMapper {

    @Mapping(source = "id", target = "id.value")
    @Mapping(source = "name", target = "name")
    DocumentTypeModel toDomain(DocumentType documentType);

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "name", target = "name")
    DocumentType toEntity(DocumentTypeModel documentTypeModel);
}
