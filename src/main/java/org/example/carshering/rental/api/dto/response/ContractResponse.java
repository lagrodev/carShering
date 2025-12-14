package org.example.carshering.rental.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.annotation.After;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Contract information response")
public record ContractResponse(

        @Schema(description = "Unique contract identifier", example = "1")
        @NotNull Long id,
        
        @Schema(description = "Total cost of the rental contract", example = "15000.0")
        @NotNull BigDecimal totalCost,
        
        @Schema(description = "Brand of the rented car", example = "Toyota")
        @NotBlank String brand,
        
        @Schema(description = "Model of the rented car", example = "Camry")
        @NotBlank String model,
        
        @Schema(description = "Body type of the rented car", example = "Sedan")
        @NotBlank String bodyType,
        
        @Schema(description = "Class of the rented car", example = "Business")
        @NotBlank String carClass,
        
        @Schema(description = "Year of manufacture of the rented car", example = "2020")
        @NotNull Integer yearOfIssue,
        
        @Schema(description = "Last name of the client renting the car", example = "Smith")
        @NotBlank String lastName,
        
        @Schema(description = "Contract start date and time", example = "2025-12-01T10:00:00")
        @FutureOrPresent @NotNull LocalDateTime startDate,
        
        @Schema(description = "Contract end date and time", example = "2025-12-15T10:00:00")
        @FutureOrPresent @NotNull @After("startDate") LocalDateTime endDate,
        
        @Schema(description = "Vehicle Identification Number of the rented car", example = "1HGBH41JXMN109186")
        @NotBlank String vin,
        
        @Schema(description = "State registration number of the rented car", example = "A123BC")
        @NotBlank String gosNumber,
        
        @Schema(description = "Current state of the contract", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "COMPLETED", "CANCELLED"})
        @NotBlank String state

) {
}
