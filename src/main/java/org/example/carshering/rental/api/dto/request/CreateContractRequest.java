package org.example.carshering.rental.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to create a new rental contract")
public record CreateContractRequest(
        @Schema(description = "Unique identifier of the car to rent", example = "1")
        @NotNull Long carId,
        
        @Schema(description = "Start date and time of the rental period", example = "2025-12-01T10:00:00")
        @FutureOrPresent @NotNull LocalDateTime dataStart,
        
        @Schema(description = "End date and time of the rental period (must be after start date)", example = "2025-12-15T10:00:00")
        @FutureOrPresent @NotNull LocalDateTime dataEnd
) {}