package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Detailed car information response")
public record CarDetailResponse(
        @Schema(description = "Car ID", example = "1")
        Long id,
        @Schema(description = "Model ID", example = "1")
        Long modelId,
        @Schema(description = "Car brand", example = "Toyota")
        String brand,
        @Schema(description = "Car model", example = "Camry")
        String model,
        @Schema(description = "Body type", example = "Sedan")
        String bodyType,
        @Schema(description = "Car class", example = "Business")
        String carClass,
        @Schema(description = "Year of issue", example = "2020")
        Integer yearOfIssue,
        @Schema(description = "State registration number", example = "A123BC")
        String gosNumber,
        @Schema(description = "Vehicle Identification Number", example = "1HGBH41JXMN109186")
        String vin,
        @Schema(description = "Car status", example = "AVAILABLE")
        String status,
        @Schema(description = "Rental price per day", example = "2500.0")
        Double rent,
        @Schema(description = "Is the car in user's favorites", example = "true")
        boolean favorite
) {}
