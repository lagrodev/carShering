package org.example.carshering.favorites.domain.valueobject;

import java.util.Objects;


public record FavoriteId(Long value) {
    public FavoriteId {
        Objects.requireNonNull(value, "FavoriteId cannot be null");
    }
}