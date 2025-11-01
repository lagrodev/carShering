package org.example.carshering.dto.request.create;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateContractRequest(
        @NotNull Long carId,
        @Future @NotNull LocalDate dataStart,
        @Future @NotNull LocalDate dataEnd
) {}