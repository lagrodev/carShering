package org.example.carshering.repository;

import org.example.carshering.domain.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelNameRepository extends JpaRepository<Model,Long> {
    Optional<Model> findByNameIgnoreCase(String name);
}
