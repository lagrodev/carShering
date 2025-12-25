package org.example.carshering.favorites.application.dto;

import java.time.Instant;

public record FavoriteDto(
        Long id,
        Long carId,
        Long userId,
        Instant createdAt
) {

}
