package org.example.carshering.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to filter contracts")
public record FilterContractRequest(
        @Schema(description = "Filter by contract status", example = "ACTIVE")
        String status,   // true = только заблокированные, false = только активные, null = все
        @Schema(description = "Filter by user ID", example = "1")
        Long idUser,
        @Schema(description = "Filter by car ID", example = "1")
        Long idCar,
        @Schema(description = "Filter by car brand", example = "Toyota")
        String brand,
        @Schema(description = "Filter by body type", example = "Sedan")
        String bodyType,
        @Schema(description = "Filter by car class", example = "Economy")
        String carClass
) {}
/*

{
  "banned": true,
  "roleName": null
}
*/
