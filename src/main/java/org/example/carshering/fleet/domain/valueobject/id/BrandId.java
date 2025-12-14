package org.example.carshering.fleet.domain.valueobject.id;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record BrandId(Long value) {
    public BrandId {
        Objects.requireNonNull(value, "BrandId cannot be null");
    }
}