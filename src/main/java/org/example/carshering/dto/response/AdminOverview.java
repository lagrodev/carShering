package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Admin dashboard overview statistics")
@Builder
public record AdminOverview(
        @Schema(description = "Total minutes of all rides (all time)", example = "125000")
        Long allRidesMinute,

        @Schema(description = "Total minutes of all rides this month", example = "15000")
        Long allRidesMinuteThisMonth,

        @Schema(description = "Total number of registered users", example = "250")
        Long totalUsers,

        @Schema(description = "Total number of cars in the fleet", example = "50")
        Long totalCars,

        @Schema(description = "Total number of contracts (all time)", example = "1500")
        Long allContracts,

        @Schema(description = "Total number of contracts this month", example = "120")
        Long allContractsMonth,

        @Schema(description = "Number of active users (users with recent activity)", example = "85")
        Long totalActiveUsers,

        @Schema(description = "Number of available cars ready for rent", example = "35")
        Long totalAvailableCars,

        @Schema(description = "Total profit in rubles (all time)", example = "2500000.00")
        Double profit,

        @Schema(description = "Total profit this month in rubles", example = "180000.00")
        Double profitThisMonth
) {
}
