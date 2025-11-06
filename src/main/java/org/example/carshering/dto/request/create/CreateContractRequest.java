package org.example.carshering.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to create a new contract")
public record CreateContractRequest(
        @Schema(description = "ID of the car to rent", example = "1")
        @NotNull Long carId,
        @Schema(description = "Start date of the contract", example = "2025-12-01")
        @FutureOrPresent @NotNull LocalDate dataStart,
        @Schema(description = "End date of the contract", example = "2025-12-15")
        @FutureOrPresent @NotNull LocalDate dataEnd
) {}