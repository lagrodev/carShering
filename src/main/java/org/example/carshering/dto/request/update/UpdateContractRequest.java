package org.example.carshering.dto.request.update;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Request to update rental contract dates")
public record UpdateContractRequest(
        @Schema(description = "Updated start date and time of the rental period", example = "2025-12-01T10:00:00")
        @FutureOrPresent @NotNull LocalDateTime dataStart,
        
        @Schema(description = "Updated end date and time of the rental period (must be after start date)", example = "2025-12-15T10:00:00")
        @FutureOrPresent @NotNull LocalDateTime dataEnd
) {}