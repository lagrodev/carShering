package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query(
            """
                                select f from Favorite f
                                where f.client.value = :userId
                    """
    )
    List<Favorite> findByClientId(Long userId);

    @Modifying
    @Query(
            """
                                delete from Favorite f
                                where f.client.value = :clientId and f.car.value = :carId
                    """
    )
    void deleteFavoriteByClientIdAndCarId(Long clientId, Long carId);

    @Query(
            """
                                select f from Favorite f
                                where f.client.value = :userId and f.car.value = :carId
                    """
    )
    Optional<Favorite> findByClientIdAndCarId(Long userId, Long carId);
}
