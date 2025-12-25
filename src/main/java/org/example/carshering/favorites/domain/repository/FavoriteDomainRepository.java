package org.example.carshering.favorites.domain.repository;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.example.carshering.favorites.domain.model.FavoriteCar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteDomainRepository {

    @Transactional(readOnly = true)
    Optional<FavoriteCar> getFavoriteCarByClientAndCar(ClientId clientId, CarId carId);

    @Transactional(readOnly = true)
    Page<FavoriteCar> getFavoriteCarsByClient(ClientId clientId, Pageable pageable);

    @Transactional(readOnly = true)
    Set<Long> getFavoriteCarsByClient(ClientId clientId);

    @Transactional(readOnly = true)
    Page<FavoriteCar> getFavoriteCars(Pageable pageable);

    @Transactional(readOnly = true)
    Page<FavoriteCar> getFavoriteCarByCar(CarId carId, Pageable pageable);

    boolean existsByClientAndCar(ClientId clientId, CarId carId);

    @Transactional
    FavoriteCar save(FavoriteCar favoriteCar);

    @Transactional
    List<FavoriteCar> saveAll(List<FavoriteCar> favoriteCars);

    @Transactional
    void delete(ClientId clientId, CarId carId);
}
