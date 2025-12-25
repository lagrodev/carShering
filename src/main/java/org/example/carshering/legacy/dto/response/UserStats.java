package org.example.carshering.legacy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "User statistics and analytics response")
@Builder
public record UserStats(

        @Schema(description = "ID of user's favorite or most used car", example = "5")
        Long favoriteCarId,
        
        @Schema(description = "Brand of user's favorite car", example = "Toyota")
        String favoriteCarBrand,
        
        @Schema(description = "Model name of user's favorite car", example = "Camry")
        String favoriteCarModelName,
        
        @Schema(description = "Class of user's favorite car", example = "Business")
        String favoriteCarCarClass,
        
//        @Schema(description = "Image URL of user's favorite car", example = "https://example.com/images/car.jpg")
//        String favoriteCarImageUrl,

        @Schema(description = "Total number of completed rides by user", example = "25")
        int totalRides,
        
        @Schema(description = "Number of rides completed in the current month", example = "5")
            Long ridesThisMonth,
        
        @Schema(description = "Total amount spent on rentals in rubles", example = "75000")
        Long totalSpent,
        
        @Schema(description = "User's most frequently rented car brand", example = "BMW")
        String favoriteBrand,
        
        @Schema(description = "User's most frequently rented car class", example = "Premium")
        String topUsedCarClass,
        
        @Schema(description = "Date of user's last completed ride", example = "2025-11-20")
        LocalDate lastRideDate,

        @Schema(description = "Average duration of user's rides in hours", example = "3.5")
        Double averageTimeDrive,
        
        @Schema(description = "Average time when user typically starts rides", example = "14:30:00")
        LocalTime averageTimeToStartDrive,
        
        @Schema(description = "Average cost per hour of rental for user", example = "850.0")
        Double averageCost

) {
}
