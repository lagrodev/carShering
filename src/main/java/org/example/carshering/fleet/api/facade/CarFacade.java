package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.api.mapper.CarApiMapper;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.example.carshering.service.interfaces.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Фасад для сборки CarResponse из данных Car + Favorite
 * Запрашивает данные из других контекстов и передает в маппер
 */
@Component
@RequiredArgsConstructor
public class CarFacade {

    private final FavoriteService favoriteService;
    private final CarApiMapper carApiMapper;

    /**
     * Собрать CarDetailResponse из CarDto + информация о favorite
     */
    public CarDetailResponse toDetailResponse(CarDto carDto, Long userId) {
        // Запрашиваем данные о favorite
        boolean isFavorite = false;
        if (userId != null) {
            Set<Long> favoriteCarIds = favoriteService.getAllFavoriteCarIds(userId);
            isFavorite = favoriteCarIds.contains(carDto.id());
        }

        // Передаем все в маппер
        return carApiMapper.toDetailResponse(carDto, isFavorite);
    }


    /**
     * Собрать CarDetailResponse из CarDto + информация о favorite
     */
    public CarDetailResponse toDetailResponse(CarDto carDto) {
        // Запрашиваем данные о favorite


        // Передаем все в маппер
        return carApiMapper.toDetailResponse(carDto, false);
    }

    /**
     * Собрать CarListItemResponse из CarDto + информация о favorite
     */
    public CarListItemResponse toListItemResponse(CarDto carDto, Long userId) {
        // Запрашиваем данные о favorite
        boolean isFavorite = false;
        if (userId != null) {
            Set<Long> favoriteCarIds = favoriteService.getAllFavoriteCarIds(userId);
            isFavorite = favoriteCarIds.contains(carDto.id());
        }

        // Передаем все в маппер
        return carApiMapper.toListItemResponse(carDto, isFavorite);
    }

    /**
     * Собрать Page<CarListItemResponse> с информацией о favorite
     */
    public Page<CarListItemResponse> toListItemResponsePage(Page<CarDto> carDtos, Long userId) {
        // Запрашиваем все избранные автомобили одним запросом
        Set<Long> favoriteCarIds = userId != null
            ? favoriteService.getAllFavoriteCarIds(userId)
            : Set.of();

        // Маппим каждый элемент
        return carDtos.map(carDto -> {
            boolean isFavorite = favoriteCarIds.contains(carDto.id());
            return carApiMapper.toListItemResponse(carDto, isFavorite);
        });
    }


    public Page<CarListItemResponse> toListItemResponsePage(Page<CarDto> carDtos) {
        // Запрашиваем все избранные автомобили одним запросом


        // Маппим каждый элемент
        return carDtos.map(carDto -> {
            return carApiMapper.toListItemResponse(carDto, false);
        });
    }
}

