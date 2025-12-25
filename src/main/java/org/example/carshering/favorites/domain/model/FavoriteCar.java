package org.example.carshering.favorites.domain.model;

import lombok.Getter;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.domain.valueobject.FavoriteId;

import java.time.Instant;

@Getter
public class FavoriteCar {
    private final FavoriteId favoriteId;
    private final CarId carId;
    private final ClientId clientId;
    private final Instant createdAt; // добавь timestamp


    private FavoriteCar(
            FavoriteId favoriteId,  CarId carId, ClientId clientId, Instant createdAt
    ){
        this.favoriteId = favoriteId;
        this.carId = carId;
        this.clientId = clientId;
        this.createdAt = createdAt;
    }


    public static FavoriteCar create(CarId carId, ClientId clientId) {
        return new FavoriteCar(null, carId, clientId, Instant.now());
    }

    public static FavoriteCar reconstruct(FavoriteId favoriteId, CarId carId, ClientId clientId,   Instant createdAt) {
        return new FavoriteCar(favoriteId, carId, clientId, createdAt);
    }

    public boolean isSameCarAndClient(CarId carId, ClientId clientId) {
        return this.carId.equals(carId) && this.clientId.equals(clientId);
    }

}
