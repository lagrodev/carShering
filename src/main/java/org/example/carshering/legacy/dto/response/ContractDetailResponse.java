package org.example.carshering.legacy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Detailed contract information response")
public record ContractDetailResponse(
        @Schema(description = "Full name of the client", example = "Иван Иванов")
        String clientName,

        @Schema(description = "Client's login username", example = "ivan_ivanov")
        String login,

        @Schema(description = "Car brand", example = "Toyota")
        String carBrand,

        @Schema(description = "Car class/category", example = "Business")
        String CarClass,

        @Schema(description = "Unique car identifier", example = "42")
        Long carId,

        @Schema(description = "Car model name", example = "Camry")
        String carModel,

        @Schema(description = "Total cost of the rental in rubles", example = "5500.00")
        BigDecimal totalCost,

        @Schema(description = "Duration of rental in minutes", example = "480")
        Long durationMinutes,

        @Schema(description = "Contract start date and time", example = "2025-11-27T10:00:00")
        LocalDateTime dataStart,

        @Schema(description = "Contract end date and time", example = "2025-11-27T18:00:00")
        LocalDateTime dataEnd
) {
}
