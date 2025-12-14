package org.example.carshering.fleet.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для автомобиля
 */
public record CarDto(
        Long id,
        String gosNumber,
        String vin,
        BigDecimal dailyRate,
        String currency,
        Integer year,
        String state,
        CarModelDto model,
        List<ImageDto> images
) {}

