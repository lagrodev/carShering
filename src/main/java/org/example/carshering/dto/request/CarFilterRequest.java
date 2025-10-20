package org.example.carshering.dto.request;

public record CarFilterRequest(
        String brand,
        String model,
        Integer minYear,
        Integer maxYear,
        String bodyType,
        String carClass
) {}
