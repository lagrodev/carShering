package org.example.carshering.fleet.api.dto.responce;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Brand response")
public record BrandResponse(
        @Schema(description = "Brand ID", example = "1")
        Long id,

        @Schema(description = "Brand name", example = "Toyota")
        String name

) {

}







