package org.example.carshering.favorites.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.application.dto.FavoriteDto;
import org.example.carshering.favorites.application.service.FavoriteApplicationService;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.api.mapper.CarApiMapper;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Фасад favorites BC
 * Маппинг FavoriteDto → CarListItemResponse + публичный API для других BC
 */
@RequiredArgsConstructor
@Component
public class FavoriteFacade {
    
    private final FavoriteApplicationService favoriteAppService;
    private final CarApplicationService carAppService;
    private final CarApiMapper carApiMapper;

    /**
     * Маппинг FavoriteDto → CarListItemResponse
     * favorite=true (т.к. это FavoriteFacade, машина УЖЕ в избранном)
     */
    public CarListItemResponse getAllListItemForCar(FavoriteDto favorite) {
        var carDto = carAppService.getCarById(new CarId(favorite.carId()));
        
        return carApiMapper.toListItemResponse(carDto, true);
    }

    /**
     * Публичный API для других BC (CarFacade, RentalFacade, etc.)
     * Проверить, в избранном ли машина
     */
    public boolean isFavorite(ClientId clientId, CarId carId) {
        return favoriteAppService.existsByClientAndCar(carId, clientId);
    }

    /**
     * Публичный API для CarFacade (batch-запрос)
     * Получить все ID избранных машин клиента
     */
    public Set<Long> getFavoriteCarIds(ClientId clientId) {
        return favoriteAppService.getFavoriteCarIdsByClient(clientId);
    }
}

