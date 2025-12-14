package org.example.carshering.identity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;

@Getter
@AllArgsConstructor
public class DocumentTypeModel {
    private final DocumentTypeId id;
    private final String name;
    
    public static DocumentTypeModel reconstruct(DocumentTypeId id, String name) {
        return new DocumentTypeModel(id, name);
    }
}
