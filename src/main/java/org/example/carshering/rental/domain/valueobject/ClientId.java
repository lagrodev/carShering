package org.example.carshering.rental.domain.valueobject;


import java.util.Objects;

// common/domain/valueobject/ClientId.java
public record ClientId(Long value) {
    public ClientId {
        Objects.requireNonNull(value, "ClientId cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("ClientId must be positive");
        }
    }
}
