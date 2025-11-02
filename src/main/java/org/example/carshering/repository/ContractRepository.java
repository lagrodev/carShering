package org.example.carshering.repository;

import org.example.carshering.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query(
            """
                    SELECT c FROM Contract c
                        WHERE c.car.id = :carId
                            AND c.state.name IN ('BOOKED', 'ACTIVE', 'PENDING')
                            AND c.dataEnd > :startDate
                            AND c.dataStart < :endDate
                    """
    )
    List<Contract> findOverlappingContracts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("carId") Long carId);

    @Query(
            """
                    SELECT c FROM Contract c
                        WHERE c.client.id = :userId
                            AND c.id = :contractId
                    """
    )
    Optional<Contract> findByIdAndUserId(@Param("contractId") Long contractId, @Param("userId") Long userId);

    Page<Contract> findByClientId(Long userId, Pageable pageable);

    @Query("""
    SELECT c FROM Contract c
    JOIN c.car car
    JOIN car.model model
    WHERE (:status IS NULL OR c.state.name = :status)
      AND (:idUser IS NULL OR c.client.id = :idUser)
      AND (:idCar IS NULL OR c.car.id = :idCar)
      AND (:brand IS NULL OR model.brand.name = :brand)
      AND (:bodyType IS NULL OR model.bodyType = :bodyType)
      AND (:carClass IS NULL OR model.carClass.name = :carClass)
    """)
    Page<Contract> findAllByFilter(
            @Param("status") String status,
            @Param("idUser") Long idUser,
            @Param("idCar") Long idCar,
            @Param("brand") String brand,
            @Param("bodyType") String bodyType,
            @Param("carClass") String carClass,
            Pageable pageable
    );
}
