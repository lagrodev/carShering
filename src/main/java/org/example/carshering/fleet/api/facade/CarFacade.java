package org.example.carshering.fleet.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.api.facade.FavoriteFacade;
import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.api.mapper.CarApiMapper;
import org.example.carshering.fleet.application.dto.response.CarDto;
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

    private final FavoriteFacade favoriteFacade;
    private final CarApiMapper carApiMapper;

    /**
     * Собрать CarDetailResponse из CarDto + информация о favorite
     */
    public CarDetailResponse toDetailResponse(CarDto carDto, Long userId) {
        // Запрашиваем данные о favorite
        boolean isFavorite = favoriteFacade.isFavorite(new ClientId(userId), new CarId(carDto.id()));
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
        boolean isFavorite = favoriteFacade.isFavorite(new ClientId(userId), new CarId(carDto.id()));

        // Передаем все в маппер
        return carApiMapper.toListItemResponse(carDto, isFavorite);
    }

    public Page<CarListItemResponse> toListItemResponsePage(
            Page<CarDto> carDtos,
            Long userId
    ) {
        if (userId == null) {
            return carDtos.map(carDto ->
                    carApiMapper.toListItemResponse(carDto, false)
            );
        }

        Set<Long> favoriteCarIds = favoriteFacade.getFavoriteCarIds(
                new ClientId(userId)
        );

        // Маппим с уже загруженными данными (проверка в памяти)
        return carDtos.map(carDto -> {
            boolean isFavorite = favoriteCarIds.contains(carDto.id());
            return carApiMapper.toListItemResponse(carDto, isFavorite);
        });
    }


    /**
     * Собрать Page<CarListItemResponse> с информацией о favorite
     */
//    public Page<CarListItemResponse> toListItemResponsePage(Page<CarDto> carDtos, Long userId) {
//
//        // Маппим каждый элемент
//        return carDtos.map(carDto -> {
//            boolean isFavorite = favoriteFacade.isFavorite(new ClientId(userId), new CarId(carDto.id()));;
//            return carApiMapper.toListItemResponse(carDto, isFavorite);
//        });
//    }
    public Page<CarListItemResponse> toListItemResponsePage(Page<CarDto> carDtos) {
        // Запрашиваем все избранные автомобили одним запросом


        // Маппим каждый элемент
        return carDtos.map(carDto -> {
            return carApiMapper.toListItemResponse(carDto, false);
        });
    }
}

