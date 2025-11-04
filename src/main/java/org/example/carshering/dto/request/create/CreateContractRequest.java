package org.example.carshering.dto.request.create;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateContractRequest(
        @NotNull Long carId,
        @FutureOrPresent @NotNull LocalDate dataStart,
        @FutureOrPresent @NotNull LocalDate dataEnd
) {}