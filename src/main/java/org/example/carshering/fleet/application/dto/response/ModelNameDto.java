package org.example.carshering.fleet.application.dto.response;

/**
 * DTO для названия модели (Camry, Corolla, X5)
 */
public record ModelNameDto(
        Long id,
        String name
) {}

