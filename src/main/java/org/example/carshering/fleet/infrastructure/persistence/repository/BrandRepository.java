package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {

    @Query(
        "SELECT b FROM Brand b WHERE LOWER(b.name) = LOWER(:name)"
    )
    Optional<Brand> findByNameIgnoreCase(String name);
}
