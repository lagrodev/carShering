package org.example.carshering.dto.request.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateCarModelRequest(

        @NotBlank String brand,

        @NotBlank String model,

        @NotBlank String bodyType,

        @NotBlank String carClass
) {
}
