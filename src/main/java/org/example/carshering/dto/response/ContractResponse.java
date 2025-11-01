package org.example.carshering.dto.response;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.annotation.After;

import java.time.LocalDate;

public record ContractResponse(
        @NotNull Long id,
        @NotNull Double totalCost,
        @NotNull String brand,
        @NotNull String model,
        @NotNull String bodyType,
        @NotNull String carClass,
        @NotNull Integer yearOfIssue,
        @NotNull String lastName,
        @Future @NotNull LocalDate startDate,
        @Future @NotNull @After("startDate") LocalDate endDate,
        @NotBlank String vin,
        @NotBlank String gosNumber,
        @NotNull String state

) {
}
