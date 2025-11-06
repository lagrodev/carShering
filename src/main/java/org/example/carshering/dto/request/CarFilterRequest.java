package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
        List<String> carState
) {
}
