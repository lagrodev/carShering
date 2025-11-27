package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car analytics statistics response")
public record CarAnalyticsResponse(
        @Schema(description = "Unique car identifier", example = "42")
        Long carId,

        @Schema(description = "Car government registration number", example = "А123АА777")
        String gosNumber,

        @Schema(description = "Car VIN number", example = "1HGBH41JXMN109186")
        String vin,

        @Schema(description = "Car brand", example = "Toyota")
        String brand,

        @Schema(description = "Car model name", example = "Camry")
        String modelName,

        @Schema(description = "Car class/category", example = "Business")
        String carClass,

        @Schema(description = "Year of manufacture", example = "2022")
        Integer yearOfIssue,

//        @Schema(description = "Car image URL", example = "https://example.com/car.jpg")
//        String imageUrl,

        @Schema(description = "Hourly rental rate in rubles", example = "500.00")
        Double rentPerHour,

        @Schema(description = "Total mileage in minutes (rental time)", example = "12000")
        Long totalMileageMinutes,

        @Schema(description = "Average earnings per rental in rubles", example = "3500.50")
        Double averageEarnings,

        @Schema(description = "Number of unique users who rented this car", example = "15")
        Long uniqueUsersCount,

        @Schema(description = "Total rental time in minutes within the period", example = "5400")
        Long rentalTimeMinutes,

        @Schema(description = "Total profit generated within the period in rubles", example = "45000.00")
        Double totalProfit
) {
}

