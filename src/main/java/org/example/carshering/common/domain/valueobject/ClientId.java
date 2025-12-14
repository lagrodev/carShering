package org.example.carshering.common.domain.valueobject;


import jakarta.persistence.Embeddable;
import org.example.carshering.common.exceptions.custom.NotFoundException;

import java.util.Objects;

@Embeddable
public record ClientId(Long value) {
    public ClientId {
        Objects.requireNonNull(value, "ClientId cannot be null");
        if (value <= 0) {
            throw new NotFoundException("ClientId must be positive");
        }
    }
}
