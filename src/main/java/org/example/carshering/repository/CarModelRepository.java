package org.example.carshering.repository;

import ch.qos.logback.core.model.Model;
import org.example.carshering.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;



// todo пагинацию добавить
// todo сделать поиск по подстроке
@Repository
public interface CarModelRepository extends JpaRepository<CarModel,Long> {
    List<CarModel> findAllByDeletedFalse();

    Optional<CarModel> findByIdAndDeletedFalse(Long id);

    @Query("""
        SELECT m FROM CarModel m
        WHERE (:brand IS NULL OR m.brand ILIKE %:brand%)
          AND (:bodyType IS NULL OR m.bodyType ILIKE %:bodyType%)
          AND (:carClass IS NULL OR m.carClass ILIKE %:carClass%)
          AND m.deleted = false
        ORDER BY m.brand, m.model
    """)
    List<CarModel> findModelsByFilter(
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass
    );



    @Query("""
        SELECT m FROM CarModel m
        WHERE (:brand IS NULL OR m.brand ILIKE %:brand%)
          AND (:bodyType IS NULL OR m.bodyType ILIKE %:bodyType%)
          AND (:carClass IS NULL OR m.carClass ILIKE %:carClass%)
        ORDER BY m.brand, m.model
    """)
    List<CarModel> findModelsByFilterIncludingDeleted(
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass
    );
}
