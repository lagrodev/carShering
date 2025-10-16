package org.example.carshering.repository;

import org.example.carshering.entity.CarState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarStateRepository extends JpaRepository<CarState, Integer> {
    Optional<CarState> findByStatus(String available);
}
