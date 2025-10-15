package org.example.carshering.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {


    @Query("SELECT c FROM Car c WHERE c.state.status = 'AVAILABLE'")
    List<Car> findValidCars();

    boolean existsByGosNumber(@NotBlank String s);

    boolean existsByVin(@NotBlank String vin);
}
