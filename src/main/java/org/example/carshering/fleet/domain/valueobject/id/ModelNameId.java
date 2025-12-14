package org.example.carshering.fleet.domain.valueobject.id;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record ModelNameId(Long value) {
    public ModelNameId {
        Objects.requireNonNull(value, "ModelNameId cannot be null");
    }
}