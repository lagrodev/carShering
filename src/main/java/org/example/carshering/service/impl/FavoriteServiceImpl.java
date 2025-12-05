package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.Favorite;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.mapper.CarMapper;
import org.example.carshering.repository.FavoriteRepository;
import org.example.carshering.service.interfaces.CarService;
import org.example.carshering.service.interfaces.ClientService;
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
    private final CarMapper carMapper;
    private final ClientService clientService;
    private final CarService carService;

    @Override
    public Page<CarListItemResponse> getAllFavorites(Long userId, Pageable pageable) {


        List<Favorite> list = favoriteRepository.findByClientId(userId);
        List<CarListItemResponse> carListItemResponses = list.stream()
                .map(
                        favorite -> {
                            Car car = carService.getEntity(favorite.getCar().getId());
                            return carMapper.toListItemDto(car, true);
                        }
                ).toList();
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

        Client client = clientService.getEntity(userId);
        Car car = carService.getEntity(carId);
        favoriteRepository.findByClientIdAndCarId(userId, carId)
                .ifPresent(favorite -> {
                    throw new AlreadyExistsException("Favorite already exists for userId: " + userId + " and carId: " + carId);
                });


        favoriteRepository.save(
                Favorite
                        .builder()
                        .client(client)
                        .car(car)
                        .build()
        );

        return carMapper.toListItemDto(car, true);

    }

    @Override
    public CarListItemResponse getFavorite(Long userId, Long carId) {

        return favoriteRepository.findByClientIdAndCarId(userId, carId).map(
                favoriteResponse -> {
                    Car car = carService.getEntity(favoriteResponse.getCar().getId());
                    return carMapper.toListItemDto(car, true);
                }
        ).orElse(null);
    }

    @Override
    public Set<Long> getAllFavoriteCarIds(Long clientId) {
        List<Favorite> list = favoriteRepository.findByClientId(clientId);
        return list.stream()
                .map(favorite -> favorite.getCar().getId())
                .collect(java.util.stream.Collectors.toSet());
    }


}
