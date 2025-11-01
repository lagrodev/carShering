package org.example.carshering.dto.request.create;

import jakarta.validation.constraints.NotBlank;

public record CreateCarModelsBrand(
        @NotBlank String name
        )
{}
