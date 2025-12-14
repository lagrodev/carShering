package org.example.carshering.fleet.domain.valueobject.name;

public record BodyType(
        String value
) {
    public BodyType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BodyType cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("BodyType too long");
        }
    }
}
