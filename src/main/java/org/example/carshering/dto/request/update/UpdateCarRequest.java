package org.example.carshering.dto.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Request to update car information")
public record UpdateCarRequest(
        @Schema(description = "ID of the car model", example = "1")
        Long modelId,
        @Schema(description = "Year of issue", example = "2020")
        Integer yearOfIssue,
        @Schema(description = "State registration number", example = "A123BC")
        String gosNumber,
        @Schema(description = "Vehicle Identification Number", example = "1HGBH41JXMN109186")
        String vin,
        @Schema(description = "Rental price per day", example = "2500.0")
        @Positive Double rent
        ) {
}