package org.example.carshering.service.interfaces;

import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface FavoriteService {
    Page<CarListItemResponse> getAllFavorites(Long userId, Pageable pageable);

    void deleteFavorite(Long userId, Long carId);

    CarListItemResponse addFavorite(Long userId, Long carId);

    CarListItemResponse getFavorite(Long userId, Long carId);

    Set<Long> getAllFavoriteCarIds(Long clientId);
}
