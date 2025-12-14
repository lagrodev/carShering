package org.example.carshering.fleet.domain.valueobject.id;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record ModelId(Long value) {
    public ModelId {
        Objects.requireNonNull(value, "ModelId cannot be null");
    }
}