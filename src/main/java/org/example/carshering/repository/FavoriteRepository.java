package org.example.carshering.repository;

import org.example.carshering.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByClientId(Long userId);

    void deleteFavoriteByClientIdAndCarId(Long clientId, Long carId);

    Optional<Favorite> findByClientIdAndCarId(Long userId, Long carId);
}
