package org.example.carshering.favorites.infrastructure.persistence.repository;

import org.example.carshering.favorites.infrastructure.persistence.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query(
            """
                    select f from Favorite f
                    where f.client.value = :userId
                    """
    )
    Page<Favorite> findByClientId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            """
        select f.car.value from Favorite f
        where f.client.value = :userId
"""
    )
    Set<Long> findCarByClientId(@Param("userId") Long userId);

    @Modifying
    @Query(
            """
                        delete from Favorite f
                        where f.client.value = :userId and f.car.value = :carId
                    """
    )
    void deleteFavoriteByClientIdAndCarId(@Param("userId") Long userId, @Param("carId") Long carId);

    @Query(
            """
                    select f from Favorite f
                    where f.client.value = :userId and f.car.value = :carId
                    """
    )
    Optional<Favorite> findByClientIdAndCarId(@Param("userId") Long userId, @Param("carId") Long carId);


    @Query(
            """
                            select f from Favorite f
                            where f.car.value = :carId
                    """
    )
    Page<Favorite> getFavoritesByCar(@Param("carId") Long carId, Pageable pageable);

    @Query(
            """
                select (count(f) > 0) from Favorite f where f.client.value = :clientId and f.car.value = :carId
            """
    )
    boolean existsByClientAndCar(@Param("clientId") Long client, @Param("carId") Long car);
}
