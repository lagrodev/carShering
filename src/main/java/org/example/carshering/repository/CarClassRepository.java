package org.example.carshering.repository;

import org.example.carshering.entity.CarClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarClassRepository extends JpaRepository<CarClass,Long> {
    Optional<CarClass> findByNameIgnoreCase(String name);
}
