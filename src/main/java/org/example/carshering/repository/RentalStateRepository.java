package org.example.carshering.repository;

import org.example.carshering.domain.entity.RentalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalStateRepository extends JpaRepository<RentalState,Long> {
    Optional<RentalState> findByNameIgnoreCase(String name);
}
