package org.example.carshering.mapper;

import org.example.carshering.domain.entity.Document;
import org.example.carshering.domain.entity.DocumentType;
import org.example.carshering.domain.valueobject.DateOfIssue;
import org.example.carshering.domain.valueobject.DocumentNumber;
import org.example.carshering.domain.valueobject.DocumentSeries;
import org.example.carshering.domain.valueobject.IssuingAuthority;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.repository.DocumentTypeRepository;
import org.example.carshering.service.interfaces.DocumentService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DocumentMapper {

    // 2. Внедряем репозиторий (MapStruct сам подставит реализацию) ////// TODO УДАЛИТЬ НАХУЙ
    @Autowired
    protected DocumentTypeRepository documentTypeRepository;

    // 3. Метод для частичного обновления
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void update(@MappingTarget Document document, UpdateDocumentRequest request);

    // 4. Кастомный метод: MapStruct вызовет его автоматически,
    //    потому что имена совпадают: request.documentTypeId → document.documentType
    protected DocumentType documentTypeIdToDocumentType(Long documentTypeId) {
        if (documentTypeId == null) {
            return null;
        }
        return documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("DocumentType not found: " + documentTypeId));
    }

    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "series", source = "series")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "dateOfIssue", source = "dateOfIssue")
    @Mapping(target = "issuingAuthority", source = "issuingAuthority")
    public abstract Document toEntity(CreateDocumentRequest request);

    protected String documentSeriesToString(DocumentSeries series) {
        return series != null ? series.getValue() : null;
    }

    protected String documentNumberToString(DocumentNumber number) {
        return number != null ? number.getValue() : null;
    }

    protected LocalDate dateOfIssueToLocalDate(DateOfIssue dateOfIssue) {
        return dateOfIssue != null ? dateOfIssue.getValue() : null;
    }

    protected String issuingAuthorityToString(IssuingAuthority authority) {
        return authority != null ? authority.getValue() : null;
    }



    // Эти методы нужны для toEntity(CreateDocumentRequest → Document)
    protected DocumentSeries stringToDocumentSeries(String series) {
        return series != null ? DocumentSeries.of(series) : null;
    }

    protected DocumentNumber stringToDocumentNumber(String number) {
        return number != null ? DocumentNumber.of(number) : null;
    }

    protected DateOfIssue localDateToDateOfIssue(LocalDate date) {
        return date != null ? DateOfIssue.of(date) : null;
    }

    protected IssuingAuthority stringToIssuingAuthority(String authority) {
        return authority != null ? IssuingAuthority.of(authority) : null;
    }

    @Mapping(source = "documentType.name", target = "documentType")
    @Mapping(source = "series", target = "series")
    @Mapping(source = "number", target = "number")
    @Mapping(source = "dateOfIssue", target = "dateOfIssue")
    @Mapping(source = "issuingAuthority", target = "issuingAuthority")
    public abstract DocumentResponse toDto(Document document);
}

