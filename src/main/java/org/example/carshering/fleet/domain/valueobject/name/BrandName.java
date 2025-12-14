package org.example.carshering.fleet.domain.valueobject.name;

public record BrandName(
        String value
) {
    public BrandName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("BrandName cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("BrandName too long");
        }
    }
}
