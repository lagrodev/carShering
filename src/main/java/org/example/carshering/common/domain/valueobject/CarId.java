package org.example.carshering.common.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

// common/domain/valueobject/CarId.java
@Embeddable
public record CarId(Long value) {
    public CarId {
        Objects.requireNonNull(value, "CarId cannot be null");
    }
}
