package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.annotation.After;

import java.time.LocalDate;

@Schema(description = "Contract information response")
public record ContractResponse(

        @Schema(description = "Contract ID", example = "1")
        @NotNull Long id,
        @Schema(description = "Total cost of the contract", example = "15000.0")
        @NotNull Double totalCost,
        @Schema(description = "Car brand", example = "Toyota")
        @NotBlank String brand,
        @Schema(description = "Car model", example = "Camry")
        @NotBlank String model,
        @Schema(description = "Body type", example = "Sedan")
        @NotBlank String bodyType,
        @Schema(description = "Car class", example = "Business")
        @NotBlank String carClass,
        @Schema(description = "Year of issue", example = "2020")
        @NotNull Integer yearOfIssue,
        @Schema(description = "Client last name", example = "Smith")
        @NotBlank String lastName,
        @Schema(description = "Contract start date", example = "2025-12-01")
        @FutureOrPresent @NotNull LocalDate startDate,
        @Schema(description = "Contract end date", example = "2025-12-15")
        @FutureOrPresent @NotNull @After("startDate") LocalDate endDate,
        @Schema(description = "Vehicle Identification Number", example = "1HGBH41JXMN109186")
        @NotBlank String vin,
        @Schema(description = "State registration number", example = "A123BC")
        @NotBlank String gosNumber,
        @Schema(description = "Contract state", example = "ACTIVE")
        @NotBlank String state

) {
}
