package org.example.carshering.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {


    @Query("""
            SELECT c FROM Car c
            JOIN c.model m
            WHERE (:onlyAvailable IS FALSE OR c.state.status = 'AVAILABLE')
              AND (:brand IS NULL OR m.brand = :brand)
              AND (:model IS NULL OR m.model = :model)
              AND (:minYear IS NULL OR c.yearOfIssue >= :minYear)
              AND (:maxYear IS NULL OR c.yearOfIssue <= :maxYear)
              AND (:bodyType IS NULL OR m.bodyType = :bodyType)
              AND (:carClass IS NULL OR m.carClass = :carClass)
            """)
    Page<Car> findByFilter(
            @Param("onlyAvailable") boolean onlyAvailable,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );

    boolean existsByGosNumber(@NotBlank String s);

    boolean existsByVin(@NotBlank String vin);
}
