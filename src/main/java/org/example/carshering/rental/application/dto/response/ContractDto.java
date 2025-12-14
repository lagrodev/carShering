package org.example.carshering.rental.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContractDto(
        Long id,
        Long carId,
        Long clientId,
        BigDecimal totalCost,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String state
) {
}
