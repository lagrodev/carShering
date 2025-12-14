package org.example.carshering.identity.domain.valueobject.document;

import org.example.carshering.common.exceptions.custom.NotFoundException;

import java.util.Objects;

public record DocumentId(
        Long value
) {
    public DocumentId {
        Objects.requireNonNull(value, "DocumentId cannot be null");
        if (value <= 0) {
            throw new NotFoundException("DocumentId must be positive");
        }
    }
}
