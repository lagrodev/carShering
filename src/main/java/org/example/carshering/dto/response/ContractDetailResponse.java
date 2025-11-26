package org.example.carshering.dto.response;

import java.time.LocalDateTime;

public record ContractDetailResponse(
        String clientName,
        String login,
        String carBrand,
        String CarClass,
        Long carId,
        String carModel,
        Double totalCost,
        Long durationMinutes,
        LocalDateTime dataStart,
        LocalDateTime dataEnd
) {
}
