package org.example.carshering.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to filter car models")
public record FilterCarModelRequest(
        @Schema(description = "Filter by brand name", example = "Toyota")
        String brand,
        @Schema(description = "Filter by body type", example = "Sedan")
        String bodyType,
        @Schema(description = "Filter by car class", example = "Economy")
        String carClass,
        @Schema(description = "Include deleted models", example = "false")
        boolean deleted
) {}