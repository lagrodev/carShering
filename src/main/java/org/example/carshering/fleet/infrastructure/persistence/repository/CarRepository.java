package org.example.carshering.fleet.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.infrastructure.persistence.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

  @Query("""
      SELECT c FROM Car c
      WHERE (:carStates IS NULL OR c.state IN :carStates)
        AND (:brands IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            JOIN Brand b ON b.id = cm.brand.value
            WHERE cm.idModel = c.model.value
              AND b.name.value IN :brands
        ))
        AND (:models IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            JOIN Model m ON m.id = cm.model.value
            WHERE cm.idModel = c.model.value
              AND m.name.value IN :models
        ))
        AND (:minYear IS NULL OR c.yearOfIssue.value >= :minYear)
        AND (:maxYear IS NULL OR c.yearOfIssue.value <= :maxYear)
        AND (:bodyType IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            WHERE cm.idModel = c.model.value
              AND cm.bodyType = :bodyType
        ))
        AND (:carClasses IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            LEFT JOIN CarClass cc ON cc.id = cm.carClass.value
            WHERE cm.idModel = c.model.value
              AND cc.name.value IN :carClasses
        ))
        AND NOT EXISTS (
            SELECT 1 FROM ContractJpaEntity rent
            WHERE rent.carId = c.id
              AND rent.period.startDate <= :dateEnd
              AND rent.period.endDate >= :dateStart
        )
        AND (:minCell IS NULL OR :minCell <= c.dailyRate.amount)
        AND (:maxCell IS NULL OR :maxCell >= c.dailyRate.amount)
      """)
  Page<Car> findByFilter(
      @Param("brands") List<String> brands,
      @Param("models") List<String> models,
      @Param("minYear") Integer minYear,
      @Param("maxYear") Integer maxYear,
      @Param("bodyType") String bodyType,
      @Param("carClasses") List<String> carClasses,
      @Param("carStates") List<String> carStates,
      @Param("dateStart") LocalDateTime dateStart,
      @Param("dateEnd") LocalDateTime dateEnd,
      @Param("minCell") BigDecimal minCell,
      @Param("maxCell") BigDecimal maxCell,

      Pageable pageable);


  @Query(
          """
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Car c
        WHERE c.gosNumber.value = :s
"""
  )
  boolean existsByGosNumber(@NotBlank String s);

  @Query(
          """
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Car c
        WHERE c.vin.value = :vin
""")
  boolean existsByVin(@NotBlank String vin);

  @Query("""
      SELECT new org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters(
          MIN(c.dailyRate.amount),
          MAX(c.dailyRate.amount)
      )
      FROM Car c
      WHERE (:carStates IS NULL OR c.state IN :carStates)
        AND (:brands IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            JOIN Brand b ON b.id = cm.brand.value
            WHERE cm.idModel = c.model.value
              AND b.name.value IN :brands
        ))
        AND (:models IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            JOIN Model m ON m.id = cm.model.value
            WHERE cm.idModel = c.model.value
              AND m.name.value IN :models
        ))
        AND (:minYear IS NULL OR c.yearOfIssue.value >= :minYear)
        AND (:maxYear IS NULL OR c.yearOfIssue.value <= :maxYear)
        AND (:bodyType IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            WHERE cm.idModel = c.model.value
              AND cm.bodyType = :bodyType
        ))
        AND (:carClasses IS NULL OR EXISTS (
            SELECT 1 FROM CarModel cm
            LEFT JOIN CarClass cc ON cc.id = cm.carClass.value
            WHERE cm.idModel = c.model.value
              AND cc.name.value IN :carClasses
        ))
        AND NOT EXISTS (
            SELECT 1 FROM ContractJpaEntity r
            WHERE r.carId = c.id
              AND r.period.startDate <= :dateEnd
              AND r.period.endDate >= :dateStart
        )
      """)
  MinMaxCellForFilters findMinMaxPriceByFilter(
      @Param("brands") List<String> brands,
      @Param("models") List<String> models,
      @Param("minYear") Integer minYear,
      @Param("maxYear") Integer maxYear,
      @Param("bodyType") String bodyType,
      @Param("carClasses") List<String> carClasses,
      @Param("carStates") List<String> carStates,
      @Param("dateStart") LocalDateTime dateStart,
      @Param("dateEnd") LocalDateTime dateEnd);


    @Lock(LockModeType. PESSIMISTIC_WRITE)
    @Query("""
    SELECT c FROM Car c
    WHERE c.id = :carId
    """)
    Optional<Car> findByIdWithLock(@Param("carId") Long carId);


    @Query("""
        SELECT c FROM Car c
        WHERE c.id = :id AND c.state = "AVAILABLE"
    """
)
    Car findByIdAndState_Active(Long id);
}


