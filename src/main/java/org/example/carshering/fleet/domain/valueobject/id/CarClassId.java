package org.example.carshering.fleet.domain.valueobject.id;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record CarClassId(Long value) {
    public CarClassId {
        Objects.requireNonNull(value, "CarClassId cannot be null");
    }
}