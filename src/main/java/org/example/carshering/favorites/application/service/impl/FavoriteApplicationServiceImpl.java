package org.example.carshering.favorites.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.favorites.application.dto.FavoriteDto;
import org.example.carshering.favorites.application.mapper.FavoriteDtoMapper;
import org.example.carshering.favorites.application.service.FavoriteApplicationService;
import org.example.carshering.favorites.domain.model.FavoriteCar;
import org.example.carshering.favorites.domain.repository.FavoriteDomainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteApplicationServiceImpl implements FavoriteApplicationService {
    private final FavoriteDtoMapper mapper;
    private final FavoriteDomainRepository repository;


    @Override
    public FavoriteDto addFavorite(CarId carId, ClientId clientId) {
        return mapper.toDto(repository.save(FavoriteCar.create(carId, clientId)));
    }

    @Override
    public Page<FavoriteDto> getFavoriteCars(Pageable pageable) {
        return repository.getFavoriteCars(pageable).map(mapper::toDto);
    }

    @Override
    public Page<FavoriteDto> getAllFavoritesByClient(ClientId clientId, Pageable pageable) {
        return repository.getFavoriteCarsByClient(clientId, pageable).map(mapper::toDto);
    }

    @Override
    public Set<Long> getFavoriteCarIdsByClient(ClientId clientId) {
        return new HashSet<>(repository.getFavoriteCarsByClient(clientId));
    }

    @Override
    public Page<FavoriteDto> getAllFavoritesByCar(CarId carId, Pageable pageable) {
        return repository.getFavoriteCarByCar(carId, pageable).map(mapper::toDto);
    }

    @Override
    public FavoriteDto getFavoriteByClientAndCar(CarId carId, ClientId clientId) {
        return mapper.toDto( repository.getFavoriteCarByClientAndCar(clientId, carId).orElseThrow(() -> new NotFoundException("Favorite not found"

        )));
    }

    @Override
    public boolean existsByClientAndCar(CarId carId, ClientId clientId) {
        return repository.existsByClientAndCar(clientId, carId);
    }



    @Override
    public void deleteFavorite(ClientId userId, CarId carId) {
        repository.delete(userId, carId);
    }
}
