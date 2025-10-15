package org.example.carshering.dto.response;

public record CarDetailResponse(
        Long id,
        String brand,
        String model,
        String bodyType,
        String carClass,
        Integer yearOfIssue,
        String gosNumber,
        String vin,
        String status,
        Double rent
) {}
