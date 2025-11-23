package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

// CarFilterRequest.java
@Schema(description = "Request to filter cars")
public record CarFilterRequest(
        @Schema(description = "List of brand names", example = "[\"Toyota\", \"Honda\"]")
        List<String> brands,
        @Schema(description = "List of model names", example = "[\"Camry\", \"Civic\"]")
        List<String> models,
        @Schema(description = "Minimum year of issue", example = "2015")
        Integer minYear,
        @Schema(description = "Maximum year of issue", example = "2023")
        Integer maxYear,
        @Schema(description = "Body type", example = "Sedan")
        String bodyType,
        @Schema(description = "List of car classes", example = "[\"Economy\", \"Business\"]")
        List<String> carClasses,
        @Schema(description = "List of car states", example = "[\"AVAILABLE\", \"RENTED\"]")
        List<String> carState,
        @Schema(description = "Start date for car availability filter", example = "2025-01-01")
        LocalDate dateStart,
        @Schema(description = "End date for car availability filter", example = "2025-01-31")
        LocalDate dateEnd,
        @Schema(description = "Minimum price per day", example = "1000")
        Double minCell,
        @Schema(description = "Maximum price per day", example = "5000")
        Double maxCell
) {
}
