package org.example.carshering.fleet.domain.valueobject.name;

import org.example.carshering.common.exceptions.custom.BusinessException;

public record CarClassName(
        String value
) {
    public CarClassName {
        if (value == null || value.isBlank()) {
            throw new BusinessException("CarClassName cannot be empty");
        }
        if (value.length() > 30) {
            throw new BusinessException("CarClassName too long (max 30 characters)");
        }
        if (!value.matches("^[\\w\\s]+$")) {
            throw new IllegalArgumentException("CarClassName can only contain letters, digits, spaces, and underscores");
        }
    }
}
