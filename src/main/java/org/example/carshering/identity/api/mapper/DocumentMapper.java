package org.example.carshering.identity.api.mapper;

import org.example.carshering.identity.api.dto.response.DocumentResponse;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DocumentMapper {


    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "documentSeries", target = "series")
    @Mapping(source = "documentNumber", target = "number")
    @Mapping(source = "dateOfIssue", target = "dateOfIssue")
    @Mapping(source = "issuingAuthority", target = "issuingAuthority")
    public abstract DocumentResponse toDto(DocumentDto document);
}

