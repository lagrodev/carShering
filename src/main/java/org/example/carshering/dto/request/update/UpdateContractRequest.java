package org.example.carshering.dto.request.update;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateContractRequest(
        @FutureOrPresent @NotNull LocalDate dataStart,
        @FutureOrPresent @NotNull LocalDate dataEnd
) {}