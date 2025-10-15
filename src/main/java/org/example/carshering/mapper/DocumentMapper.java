package org.example.carshering.mapper;

import org.example.carshering.dto.request.CreateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentMapper {

    @Mapping(target = "documentType", ignore = true)
    Document toEntity(CreateDocumentRequest request);


    @Mapping(source = "documentType.name", target = "documentType")
    DocumentResponse toDto(Document document);
}
