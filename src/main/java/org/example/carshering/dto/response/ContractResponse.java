package org.example.carshering.dto.response;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.annotation.After;

import java.time.LocalDate;

public record ContractResponse(

        @NotNull Long id,
        @NotNull Double totalCost,
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String bodyType,
        @NotBlank String carClass,
        @NotNull Integer yearOfIssue,
        @NotBlank String lastName,
        @FutureOrPresent @NotNull LocalDate startDate,
        @FutureOrPresent @NotNull @After("startDate") LocalDate endDate,
        @NotBlank String vin,
        @NotBlank String gosNumber,
        @NotBlank String state

) {
}
