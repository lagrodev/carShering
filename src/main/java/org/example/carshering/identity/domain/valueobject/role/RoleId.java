package org.example.carshering.identity.domain.valueobject.role;

import org.example.carshering.common.exceptions.custom.NotFoundException;

import java.util.Objects;

public record RoleId(
    Long value
) {
    public RoleId {
        Objects.requireNonNull(value, "RoleId cannot be null");
        if (value <= 0) {
            throw new NotFoundException("RoleId must be positive");
        }
    }
}
