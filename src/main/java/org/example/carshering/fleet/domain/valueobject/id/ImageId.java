package org.example.carshering.fleet.domain.valueobject.id;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record ImageId(
        String value
) {
    public ImageId {
        Objects.requireNonNull(value, "BrandId cannot be null");
    }
}
