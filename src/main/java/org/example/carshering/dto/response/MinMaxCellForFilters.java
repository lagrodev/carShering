package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Minimum and maximum cell values for filters")
public record MinMaxCellForFilters(
        @Schema(description = "Minimum cell value", example = "1000")
        Double min,
        @Schema(description = "Maximum cell value", example = "5000")
        Double max
) {
}
