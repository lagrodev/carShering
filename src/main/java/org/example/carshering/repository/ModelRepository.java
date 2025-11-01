package org.example.carshering.repository;

import org.example.carshering.entity.Brand;
import org.example.carshering.entity.CarClass;
import org.example.carshering.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface ModelRepository extends JpaRepository<Model, Long> {
    Optional<Model> findByName(String name);
}
