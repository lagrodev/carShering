package org.example.carshering.dto.response;

public record CarModelResponse(

        Long modelId
        ,String brand,

        String model,

        String bodyType,

        String carClass,
        boolean isDeleted
) {
}
