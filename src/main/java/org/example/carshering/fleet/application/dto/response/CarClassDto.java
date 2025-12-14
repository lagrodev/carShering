package org.example.carshering.fleet.application.dto.response;

/**
 * DTO для класса автомобиля (Economy, Business, Premium)
 */
public record CarClassDto(
        Long id,
        String name
) {}

