package org.example.carshering.repository;

import org.example.carshering.entity.CarModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


// todo пагинацию добавить
// todo сделать поиск по подстроке
@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findAllByDeletedFalse();


    @Query("SELECT c FROM CarModel c WHERE c.idModel = :id AND c.deleted = false")
    Optional<CarModel> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query("""
    SELECT m FROM CarModel m
    WHERE (:includeDeleted IS TRUE OR m.deleted = false)
      AND (:brand IS NULL OR LOWER(m.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS text), '%')))
      AND (:bodyType IS NULL OR LOWER(m.bodyType) LIKE LOWER(CONCAT('%', CAST(:bodyType AS text), '%')))
      AND (:carClass IS NULL OR LOWER(m.carClass) LIKE LOWER(CONCAT('%', CAST(:carClass AS text), '%')))
    """)
    Page<CarModel> findModelsByFilter(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );


}
