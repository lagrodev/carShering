package org.example.carshering.dto.request;

public record FilterCarModelRequest(
        String brand,
        String bodyType,
        String carClass,
        Boolean includeDeleted,
        String sortBy,
        String sortDirection
) {}