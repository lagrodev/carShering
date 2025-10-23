package org.example.carshering.dto.request;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateContractRequest(
        @Future @NotNull LocalDate dataStart,
        @Future @NotNull LocalDate dataEnd
) {}