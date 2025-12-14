package org.example.carshering.fleet.api.dto.responce;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Car list item response")
public record CarListItemResponse(
        @Schema(description = "Car ID", example = "1")
        Long id,
        @Schema(description = "Car brand", example = "Toyota")
        String brand,
        @Schema(description = "Car class", example = "Business")
        String carClass,     // думаю, по нему делать фильтр... в голове пиздатая идея, но пиздец, по идеи, сложно @_@
        @Schema(description = "Car model", example = "Camry")
        String model,
        @Schema(description = "Year of issue", example = "2020")
        Integer yearOfIssue,
        @Schema(description = "Rental price per day", example = "2500.0")
        BigDecimal rent,
        @Schema(description = "Car status", example = "AVAILABLE")
        String status,
        @Schema(description = "Is the car in user's favorites", example = "true")
        boolean favorite
) {}
