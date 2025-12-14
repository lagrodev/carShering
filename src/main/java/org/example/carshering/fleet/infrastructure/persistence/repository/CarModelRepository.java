package org.example.carshering.fleet.infrastructure.persistence.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarModel;
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
    LEFT JOIN Brand b ON b.id = m.brand.value
    LEFT JOIN Model model ON model.id = m.model.value
    LEFT JOIN CarClass cc ON cc.id = m.carClass.value
    WHERE (:includeDeleted IS TRUE OR m.deleted = false)
      AND ((:brand IS NULL OR :brand = '') OR LOWER(b.name.value) LIKE LOWER(CONCAT('%', :brand, '%')))
      AND ((:bodyType IS NULL OR :bodyType = '') OR LOWER(m.bodyType) LIKE LOWER(CONCAT('%', :bodyType, '%')))
      AND ((:carClass IS NULL OR :carClass = '') OR LOWER(cc.name.value) LIKE LOWER(CONCAT('%', :carClass, '%')))
    """)
    Page<CarModel> findModelsByFilter(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );


    @Query("SELECT DISTINCT m.bodyType FROM CarModel m WHERE m.deleted = false AND m.bodyType IS NOT NULL")
    List<String> findDistinctBodyTypes();

    @Query("SELECT c FROM CarModel c WHERE c.idModel = :id AND c.deleted = false")
    Optional<CarModel> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query(
            """
            select cm from CarModel cm
            left join Brand b on b.id = cm.brand.value
            left join Model m on m.id = cm.model.value
            left join CarClass cc on cc.id = cm.carClass.value
            where cm.bodyType = :bodyType
              and b.id = :brandId
              and cc.id = :carClassId
              and m.id = :modelId
"""
    )
    Optional<CarModel> findByBodyTypeAndBrand_IdAndCarClass_IdAndModel_Id(
            @Param("bodyType") String bodyType,
            @Param("brandId") Long brandId,
            @Param("carClassId") Long carClassId,
            @Param("modelId") Long modelId
    );

    /**
     * @deprecated Используйте findByBodyTypeAndBrand_IdAndCarClass_IdAndModel_Id
     */
    @Deprecated
    @Query(
            """
            select cm from CarModel cm
            left join Brand b on b.id = cm.brand.value
            left join Model m on m.id = cm.model.value
            left join CarClass cc on cc.id = cm.carClass.value
            where cm.bodyType = :s
              and b.name.value = :brand
              and cc.name.value = :s1
              and m.name.value = :model
"""
    )
    Optional<CarModel> findByBodyTypeAndBrand_NameAndCarClass_NameAndModel_Name(@NotBlank String s, @NotBlank String brand, @NotBlank String s1, @NotBlank String model);
}
