package org.example.carshering.dto.response;

import lombok.Builder;

@Builder
public record CarDetailResponse(
        Long id,
        Long modelId,
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
