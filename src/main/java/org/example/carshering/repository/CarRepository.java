package org.example.carshering.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.response.MinMaxCellForFilters;
import org.example.carshering.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
    public interface CarRepository extends JpaRepository<Car, Long> {


    @Query("""
            SELECT c FROM Car c
            JOIN c.model cm
            JOIN cm.brand b
            JOIN cm.model m
            LEFT JOIN cm.carClass cc
            WHERE (:carStates IS NULL OR c.state.status IN :carStates)
              AND (:brands IS NULL OR b.name IN :brands)
              AND (:models IS NULL OR m.name IN :models)
              AND (:minYear IS NULL OR c.yearOfIssue >= :minYear)
              AND (:maxYear IS NULL OR c.yearOfIssue <= :maxYear)
              AND (:bodyType IS NULL OR cm.bodyType = :bodyType)
              AND (:carClasses IS NULL OR cc.name IN :carClasses)
              AND (
                     :dateStart IS NULL OR :dateEnd IS NULL OR
                     NOT EXISTS (
                         SELECT 1 FROM Contract r
                         WHERE r.car.id = c.id
                           AND r.dataStart <= :dateEnd
                           AND r.dataEnd >= :dateStart
                     )
              )
              AND (:minCell is null OR :minCell <= c.rent)
              AND (:maxCell is null OR :maxCell >= c.rent)
            """)
    Page<Car> findByFilter(
            @Param("brands") List<String> brands,
            @Param("models") List<String> models,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("bodyType") String bodyType,
            @Param("carClasses") List<String> carClasses,
            @Param("carStates") List<String> carStates,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd,
            @Param("minCell")  Double minCell,
            @Param("maxCell")   Double maxCell,

            Pageable pageable
    );



    boolean existsByGosNumber(@NotBlank String s);

    boolean existsByVin(@NotBlank String vin);


    @Query(
            """
        select new org.example.carshering.dto.response.MinMaxCellForFilters(
            MIN(c.rent),
            MAX(c.rent)
        )
        from Car c
        join c.model cm
        join cm.brand b
        join cm.model m
        left join cm.carClass cc
        where (:carStates IS NULL OR c.state.status IN :carStates)
          AND (:brands IS NULL OR b.name IN :brands)
          AND (:models IS NULL OR m.name IN :models)
          AND (:minYear IS NULL OR c.yearOfIssue >= :minYear)
          AND (:maxYear IS NULL OR c.yearOfIssue <= :maxYear)
          AND (:bodyType IS NULL OR cm.bodyType = :bodyType)
          AND (:carClasses IS NULL OR cc.name IN :carClasses)
          AND (
                 :dateStart IS NULL OR :dateEnd IS NULL OR
                 NOT EXISTS (
                     SELECT 1 FROM Contract r
                     WHERE r.car.id = c.id
                       AND r.dataStart <= :dateEnd
                       AND r.dataEnd >= :dateStart
                 )
          )
"""
    )
    MinMaxCellForFilters findMinMaxPriceByFilter(
            @Param("brands") List<String> brands,
            @Param("models") List<String> models,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("bodyType") String bodyType,
            @Param("carClasses") List<String> carClasses,
            @Param("carStates") List<String> carStates,
            @Param("dateStart") LocalDate dateStart,
            @Param("dateEnd") LocalDate dateEnd
    );
}