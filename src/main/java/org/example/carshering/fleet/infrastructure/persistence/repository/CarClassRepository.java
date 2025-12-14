package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarClassRepository extends JpaRepository<CarClass,Long> {

    @Query(
            "SELECT cc FROM CarClass cc WHERE LOWER(cc.name) = LOWER(:name)"
    )
    Optional<CarClass> findByNameIgnoreCase(String name);
}
