package org.example.carshering.favorites.application.service;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.application.dto.FavoriteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface FavoriteApplicationService {
    Page<FavoriteDto> getFavoriteCars(Pageable pageable);

    Page<FavoriteDto> getAllFavoritesByClient(ClientId clientId, Pageable pageable);
    Set<Long> getFavoriteCarIdsByClient(ClientId clientId);
    Page<FavoriteDto> getAllFavoritesByCar(CarId carId, Pageable pageable);

    FavoriteDto getFavoriteByClientAndCar(CarId carId, ClientId clientId);

    boolean existsByClientAndCar(CarId carId, ClientId clientId);

    FavoriteDto addFavorite(CarId carId, ClientId clientId);


    void deleteFavorite(ClientId userId, CarId carId);


}
