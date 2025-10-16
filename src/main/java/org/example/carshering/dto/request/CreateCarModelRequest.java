package org.example.carshering.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCarModelRequest(

        @NotBlank String brand,

        @NotBlank String model,

        @NotBlank String bodyType,

        @NotBlank String carClass
) {
}
