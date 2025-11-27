package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Daily revenue statistics response")
public record DailyRevenueResponse(
        @Schema(description = "Date of the revenue record", example = "2025-11-27")
        LocalDate date,

        @Schema(description = "Total revenue for the day in rubles", example = "45000.50")
        Double revenue
) {}