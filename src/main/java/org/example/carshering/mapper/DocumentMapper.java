package org.example.carshering.mapper;

import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.repository.DocumentTypeRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class DocumentMapper {

    // 2. Внедряем репозиторий (MapStruct сам подставит реализацию)
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
    public abstract Document toEntity(CreateDocumentRequest request);


    @Mapping(source = "documentType.name", target = "documentType")
    public abstract DocumentResponse toDto(Document document);
}

