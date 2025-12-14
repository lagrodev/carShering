package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request to filter available cars by multiple criteria")
public record CarFilterRequest(
        @Schema(description = "List of car brand names to filter by", example = "[\"Toyota\", \"Honda\"]")
        List<String> brands,
        
        @Schema(description = "List of car model names to filter by", example = "[\"Camry\", \"Civic\"]")
        List<String> models,
        
        @Schema(description = "Minimum year of manufacture", example = "2015")
        Integer minYear,
        
        @Schema(description = "Maximum year of manufacture", example = "2023")
        Integer maxYear,
        
        @Schema(description = "Body type of the car", example = "Sedan")
        String bodyType,
        
        @Schema(description = "List of car classes to filter by", example = "[\"Economy\", \"Business\"]")
        List<String> carClasses,
        
        @Schema(description = "List of car states to filter by", example = "[\"AVAILABLE\", \"RENTED\"]")
        List<String> carState,
        
        @Schema(description = "Start date and time for checking car availability", example = "2025-01-01T10:00:00")
        LocalDateTime dateStart,
        
        @Schema(description = "End date and time for checking car availability", example = "2025-01-31T10:00:00")
        LocalDateTime dateEnd,
        
        @Schema(description = "Minimum rental price per day", example = "1000.0")
        BigDecimal minCell,
        
        @Schema(description = "Maximum rental price per day", example = "5000.0")
        BigDecimal maxCell
) {
}
