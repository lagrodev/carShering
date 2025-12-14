package org.example.carshering.fleet.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.carshering.fleet.application.dto.response.ImageDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request для создания автомобиля
 */
public record CreateCarRequest(
        @NotBlank(message = "Gos number cannot be blank")
        String gosNumber,

        @NotBlank(message = "VIN cannot be blank")
        String vin,

        @NotNull(message = "Daily rate cannot be null")
        @Positive(message = "Daily rate must be positive")
        BigDecimal dailyRate,

        @NotNull(message = "Year cannot be null")
        Integer year,

        @NotNull(message = "Car model ID cannot be null")
        Long carModelId,

        List<ImageDto> images
) {}



