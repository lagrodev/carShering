package org.example.carshering.favorites.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.domain.model.FavoriteCar;
import org.example.carshering.favorites.domain.repository.FavoriteDomainRepository;
import org.example.carshering.favorites.infrastructure.persistence.entity.Favorite;
import org.example.carshering.favorites.infrastructure.persistence.mapper.FavoriteMapper;
import org.example.carshering.favorites.infrastructure.persistence.repository.FavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FavoriteDomainAdapter implements FavoriteDomainRepository {

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper mapper;

    @Override
    public Page<FavoriteCar> getFavoriteCars(Pageable pageable) {
        return favoriteRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<FavoriteCar> getFavoriteCarByClientAndCar(ClientId clientId, CarId carId) {
        return favoriteRepository.findByClientIdAndCarId(clientId.value(), carId.value()).map(mapper::toDomain);
    }

    @Override
    public Page<FavoriteCar> getFavoriteCarsByClient(ClientId clientId, Pageable pageable) {
        return favoriteRepository.findByClientId(clientId.value(), pageable).map(mapper::toDomain);
    }

    @Override
    public Set<Long> getFavoriteCarsByClient(ClientId clientId) {
        return favoriteRepository.findCarByClientId(clientId.value());
    }

    @Override
    public Page<FavoriteCar> getFavoriteCarByCar(CarId carId, Pageable pageable) {
        return favoriteRepository.getFavoritesByCar(carId.value(), pageable).map((mapper::toDomain));
    }

    @Override
    public boolean existsByClientAndCar(ClientId clientId, CarId carId) {
        return favoriteRepository.existsByClientAndCar(clientId.value(),carId.value());
    }

    @Override
    @Transactional
    public FavoriteCar save(FavoriteCar favoriteCar) {
        Favorite entity = mapper.toEntity(favoriteCar);
        entity = favoriteRepository.save(entity);

        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<FavoriteCar> saveAll(List<FavoriteCar> favoriteCars) {

        return favoriteRepository.saveAll(
                favoriteCars.stream()
                        .map(mapper::toEntity)
                        .toList()
        ).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(ClientId clientId, CarId carId) {
        favoriteRepository.deleteFavoriteByClientIdAndCarId(clientId.value(), carId.value());
    }


}
