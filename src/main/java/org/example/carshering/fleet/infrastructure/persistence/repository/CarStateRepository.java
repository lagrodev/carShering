package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.CarState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarStateRepository extends JpaRepository<CarState, Integer> {
    Optional<CarState> findByStatusIgnoreCase(String available);

    Optional<CarState> findById(Long id);

    void deleteById(Long id);
}
