package org.example.carshering.identity.domain.valueobject.document;

import org.example.carshering.common.exceptions.custom.NotFoundException;

import java.util.Objects;

public record DocumentTypeId(
        Long value
) {
    public DocumentTypeId {
        Objects.requireNonNull(value, "DocumentTypeId cannot be null");
        if (value <= 0) {
            throw new NotFoundException("DocumentTypeId must be positive");
        }
    }
}