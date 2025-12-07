package org.example.carshering.rental.domain.valueobject;

import java.util.Objects;

// common/domain/valueobject/CarId.java
public record CarId(Long value) {
    public CarId {
        Objects.requireNonNull(value, "CarId cannot be null");
    }
}
