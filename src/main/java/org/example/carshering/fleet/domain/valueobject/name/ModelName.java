package org.example.carshering.fleet.domain.valueobject.name;

public record ModelName(
        String value
) {
    public ModelName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ModelName cannot be empty");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("ModelName too long (max 50 characters)");
        }
    }
}
