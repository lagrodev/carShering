package org.example.carshering.fleet.api.dto.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Request to create a new car")
public record CreateCarRequest(
        @Schema(description = "ID of the car model", example = "1")
        @NotNull Long modelId,

        @Schema(description = "Year of issue of the car", example = "2009")
        @NotNull Integer yearOfIssue,
        @Schema(description = "State registration number of the car", example = "23312")
        @NotBlank String gosNumber,
        @Schema(description = "Vehicle Identification Number (VIN) of the car", example = "123")
        @NotBlank String vin,
        @Schema(description = "Rent price of the car", example = "463746394")
        @NotNull BigDecimal rent,
        @Schema(description = "ID of the car state", example = "1")
        @NotNull Long stateId
/*
{
  "modelId": "1",
  "yearOfIssue": "2009",
  "gosNumber": "23312",
  "vin": "123",
  "rent": "463746394"
}

*/

) {
}
