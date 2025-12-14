package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.common.exceptions.custom.AlreadyExistsException;
import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.example.carshering.fleet.infrastructure.persistence.entity.Favorite;
import org.example.carshering.fleet.infrastructure.persistence.repository.FavoriteRepository;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.service.interfaces.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ClientApplicationService clientService;
    private final CarApplicationService carService;

    @Override
    public Page<CarListItemResponse> getAllFavorites(Long userId, Pageable pageable) {
        List<Favorite> list = favoriteRepository.findByClientId(userId);
        List<CarListItemResponse> carListItemResponses = list.stream()
                .map(favorite -> {
                    CarDto car = carService.getCarById(favorite.getCar());
                    // Маппинг напрямую без фасада
                    return new CarListItemResponse(
                            car.id(),
                            car.model().brand(),
                            car.model().carClass(),
                            car.model().modelName(),
                            car.year(),
                            car.dailyRate(),
                            car.state(),
                            true  // в избранном всегда true
                    );
                })
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), carListItemResponses.size());
        List<CarListItemResponse> paged = carListItemResponses.subList(start, end);

        return new PageImpl<>(paged, pageable, carListItemResponses.size());
    }

    @Override
    @Transactional
    public void deleteFavorite(Long userId, Long carId) {
        favoriteRepository.deleteFavoriteByClientIdAndCarId(userId, carId);
    }

    @Override
    public CarListItemResponse addFavorite(Long userId, Long carId) {
        ClientDto client = clientService.findUser(userId);
        CarDto car = carService.getCarById(new CarId(carId));

        favoriteRepository.findByClientIdAndCarId(userId, carId)
                .ifPresent(favorite -> {
                    throw new AlreadyExistsException("Favorite already exists for userId: " + userId + " and carId: " + carId);
                });

        favoriteRepository.save(
                Favorite.builder()
                        .client(new ClientId(client.id()))
                        .car(new CarId(car.id()))
                        .build()
        );

        // Маппинг напрямую без фасада
        return new CarListItemResponse(
                car.id(),
                car.model().brand(),
                car.model().carClass(),
                car.model().modelName(),
                car.year(),
                car.dailyRate(),
                car.state(),
                true  // только что добавили в избранное
        );
    }

    @Override
    public CarListItemResponse getFavorite(Long userId, Long carId) {
        return favoriteRepository.findByClientIdAndCarId(userId, carId)
                .map(favoriteResponse -> {
                    CarDto car = carService.getCarById(favoriteResponse.getCar());
                    // Маппинг напрямую без фасада
                    return new CarListItemResponse(
                            car.id(),
                            car.model().brand(),
                            car.model().carClass(),
                            car.model().modelName(),
                            car.year(),
                            car.dailyRate(),
                            car.state(),
                            true  // в избранном всегда true
                    );
                })
                .orElse(null);
    }

    @Override
    public Set<Long> getAllFavoriteCarIds(Long clientId) {
        List<Favorite> list = favoriteRepository.findByClientId(clientId);
        return list.stream()
                .map(favorite -> favorite.getCar().value())
                .collect(java.util.stream.Collectors.toSet());
    }
}
