package org.example.carshering.dto.response;

import lombok.Builder;

@Builder
public record AdminOverview(
        Long allRidesMinute,
        Long allRidesMinuteThisMonth,

        Long totalUsers,
        Long totalCars,
        Long allContracts,
        Long allContractsMonth,

        Long totalActiveUsers,
        Long totalAvailableCars,

        Double profit,
        Double profitThisMonth
) {
}
