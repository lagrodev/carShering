package org.example.carshering.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {


    @Query("""
    SELECT c FROM Car c
    JOIN c.model m
    WHERE (:onlyAvailable = false OR c.state.status = 'AVAILABLE')
      AND (:brands IS NULL OR m.brand IN :brands)
      AND (:models IS NULL OR m.model IN :models)
      AND (:minYear IS NULL OR c.yearOfIssue >= :minYear)
      AND (:maxYear IS NULL OR c.yearOfIssue <= :maxYear)
      AND (:bodyType IS NULL OR m.bodyType = :bodyType)
      AND (:carClasses IS NULL OR m.carClass IN :carClasses)
    """)
    Page<Car> findByFilter(
            @Param("onlyAvailable") boolean onlyAvailable,
            @Param("brands") List<String> brands,
            @Param("models") List<String> models,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("bodyType") String bodyType,
            @Param("carClasses") List<String> carClasses,
            Pageable pageable
    );

    boolean existsByGosNumber(@NotBlank String s);

    boolean existsByVin(@NotBlank String vin);
}
