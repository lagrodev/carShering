package org.example.carshering.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.domain.entity.CarModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {


    @Query("""
    SELECT m FROM CarModel m
    WHERE (:includeDeleted IS TRUE OR m.deleted = false)
      AND ((:brand IS NULL OR :brand = '') OR LOWER(m.brand.name) LIKE LOWER(CONCAT('%', :brand, '%')))
      AND ((:bodyType IS NULL OR :bodyType = '') OR LOWER(m.bodyType) LIKE LOWER(CONCAT('%', :bodyType, '%')))
      AND ((:carClass IS NULL OR :carClass = '') OR LOWER(m.carClass.name) LIKE LOWER(CONCAT('%', :carClass, '%')))
    """)
    Page<CarModel> findModelsByFilter(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );


    @Query("SELECT DISTINCT m.bodyType FROM CarModel m WHERE m.deleted = false AND m.bodyType IS NOT NULL AND m.bodyType != ''")
    List<String> findDistinctBodyTypes();

    @Query("SELECT c FROM CarModel c WHERE c.idModel = :id AND c.deleted = false")
    Optional<CarModel> findByIdAndDeletedFalse(@Param("id") Long id);

    Optional<CarModel> findByBodyTypeAndBrand_NameAndCarClass_NameAndModel_Name(@NotBlank String s, @NotBlank String brand, @NotBlank String s1, @NotBlank String model);
}
