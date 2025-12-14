package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelNameRepository extends JpaRepository<Model,Long> {
    @Query(
            "SELECT m FROM Model m WHERE LOWER(m.name) = LOWER(:name)"
    )
    Optional<Model> findByNameIgnoreCase(String name);
}
